package com.gwz.crowdfunding.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gwz.crowdfunding.BaseController;
import com.gwz.crowdfunding.bean.MD5Util;
import com.gwz.crowdfunding.bean.Member;
import com.gwz.crowdfunding.constant.AttrConst;
import com.gwz.crowdfunding.service.MemberService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Controller
public class DispatcherController extends BaseController{
	
	@Autowired
	private MemberService memberService;

	@RequestMapping("/login")
	public String login() {
		return "login";
	}
	@RequestMapping("/main")
	public String main() {
		return "main";
	}
	
	//@HystrixCommand(fallbackMethod="checkLoginError")
	@ResponseBody
	@RequestMapping("/checkLogin")
	public Object checkLogin(Member member,HttpSession session) {
		start();
		
		try {
			//查询会员信息
			Member dbmember = memberService.login(member.getLoginacct());
			if(dbmember == null) {
				fail();
			}else {
				if(dbmember.getMemberpswd().equals(MD5Util.digest(member.getMemberpswd()))){
					session.setAttribute(AttrConst.SESSION_MEMBER, dbmember);
					success();
				}
			}
			success();
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}
		
		return end();
	}
	
//	public Object checkLoginError(Member member) {
//		System.out.println("出错了！！！");
//		start();
//		fail();
//		return end();
//	}
}
