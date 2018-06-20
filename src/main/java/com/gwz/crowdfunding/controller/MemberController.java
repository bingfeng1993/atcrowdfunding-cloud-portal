package com.gwz.crowdfunding.controller;

import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.gwz.crowdfunding.BaseController;
import com.gwz.crowdfunding.bean.Cert;
import com.gwz.crowdfunding.bean.Datas;
import com.gwz.crowdfunding.bean.Member;
import com.gwz.crowdfunding.bean.MemberCert;
import com.gwz.crowdfunding.bean.Ticket;
import com.gwz.crowdfunding.constant.AttrConst;
import com.gwz.crowdfunding.service.ActService;
import com.gwz.crowdfunding.service.MemberService;

@Controller
@RequestMapping("/member")
public class MemberController extends BaseController{
	
	@Autowired
	private MemberService memberService;
	@Autowired
	private ActService actService;
	
	@ResponseBody
	@RequestMapping("/finishApply")
	public Object finishApply(HttpSession session,String authcode) {
		start();
		
		try {
			//获取会员信息
			Member loginMember = (Member)session.getAttribute(AttrConst.SESSION_MEMBER);
			
			//获取流程审批单
			Ticket t = memberService.queryTicketByMemberid(loginMember.getId());
			
			//获取邮件验证码
			//判断验证码是否正确
			if(authcode.equals(t.getAuthcode())) {
				
				//更新会员的实名认证状态
				loginMember.setAuthstatus("1");
				session.setAttribute(AttrConst.SESSION_MEMBER, loginMember);
				//变化数据库
				memberService.updateAuthstatus(loginMember);
				
				success();
			}else {
				fail();
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}
		
		return end();
	}
	
	@ResponseBody
	@RequestMapping("/sendMail")
	public Object sendMail(HttpSession session,String email) {
		start();
		
		try {
			//获取登录会员
			Member loginMember = (Member)session.getAttribute(AttrConst.SESSION_MEMBER);
			//更新email
			loginMember.setEmail(email);
			//由于是分布式的环境，所以要显示的调用一下
			 session.setAttribute(AttrConst.SESSION_MEMBER, loginMember);
			//更新会员的邮箱地址
			 memberService.updateEmail(loginMember);
			
			success();
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}
		
		return end();
	}
	
	@ResponseBody
	@RequestMapping("/uploadCerts")
	public Object uploadCerts(HttpSession session,Datas ds) {
		start();
		
		try {
			//获取登录会员信息
			Member loginMember = (Member)session.getAttribute(AttrConst.SESSION_MEMBER);
			
			List<MemberCert> mcs = ds.getMcs();
			for (MemberCert mc : mcs) {
				mc.setMemberid(loginMember.getId());
				
				//保存图片
				MultipartFile file = mc.getFile();
				//获取文件的原始文件名称(xxx.jpg)
				String fileName = file.getOriginalFilename();
				//(xxxx-xxxx-xx.jpg)
				String uuidFileName = UUID.randomUUID().toString();
				String suffix = fileName.substring(fileName.lastIndexOf("."));
				File destFile = new File("D:\\java_notes\\javaproject\\resources\\bluepic\\img\\cert\\"+uuidFileName+suffix);
				//复制
				file.transferTo(destFile);
				
				mc.setIconpath(uuidFileName+suffix);
				mc.setFile(null);
			}
			
			//数据增加到数据库
			memberService.insertMemberCerts(mcs);
			
			success();
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}
		return end();
	}
	
	@ResponseBody
	@RequestMapping("/updateBasicInfo")
	public Object updateBasicInfo(HttpSession session,Member member) {
		start();
		
		try {
			Member loginMember = (Member)session.getAttribute(AttrConst.SESSION_MEMBER);
			loginMember.setRealname(member.getRealname());
			loginMember.setCardnum(member.getCardnum());
			loginMember.setTel(member.getTel());
			memberService.updateBasicInfo(loginMember);
			session.setAttribute(AttrConst.SESSION_MEMBER, loginMember);
			
			success();
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}
		
		return end();
	}
	
	@ResponseBody
	@RequestMapping("/updateAccountType")
	public Object updateAccountType(HttpSession session,Member member) {
		start();
		
		try {
			//获取登录会员
			Member loginMember = (Member)session.getAttribute(AttrConst.SESSION_MEMBER);
			loginMember.setAccttype(member.getAccttype());
			
			//更新会员的账户信息
			memberService.updateAccountType(loginMember);
			/*
			 * 分布式环境中，由于session数据是保存在缓存服务器中
			 * 所以更新session中的数据后，应该显示的调用setAccttype方法
			 * 作用就是将缓存服务器中的数据也同时更新
			 */
			session.setAttribute(AttrConst.SESSION_MEMBER, loginMember);
			
			success();
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}
		
		return end();
	}

	@RequestMapping("/apply")
	public String apply(HttpSession session,Model model) {
		
		//获取当前会员信息
		Member loginMember = (Member)session.getAttribute(AttrConst.SESSION_MEMBER);
		
		//查询会员的流程审批单
		Ticket t = memberService.queryTicketByMemberid(loginMember.getId());
		
		//当第一次申请时，跳转到账户类型的选择页面
		if(t == null) {
			//启动流程，获取流程实例的ID
			String piid = actService.startProcessInstance(loginMember.getLoginacct());
			
			t = new Ticket();
			
			t.setMemberid(loginMember.getId());
			t.setPstep("accttype");
			t.setPiid(piid);
			t.setStatus("0");
			
			memberService.insertTicket(t);
			return "member/apply-accttype-select";
		}else {
			//根据流程页面跳转页面
			String pstep = t.getPstep();
			if("basicinfo".equals(pstep)) {
				return "member/apply-basic-info";
			}else if("cert".equals(pstep)) {
				
				//查询当前会员需要提交的证明文件列表
				List<Cert> certs = memberService.queryCertsByAccountType(loginMember.getAccttype());
				model.addAttribute("certs",certs);
				
				return "member/apply-cert-upload";
			}else if("email".equals(pstep)){
				return "member/apply-email";
			}else if("checkcode".equals(pstep)) {
				return "member/apply-checkcode";
			}else {
				return "member/apply-accttype-select";
			}
		}
	}
}
