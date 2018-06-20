package com.gwz.crowdfunding.service;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gwz.crowdfunding.bean.Cert;
import com.gwz.crowdfunding.bean.Member;
import com.gwz.crowdfunding.bean.MemberCert;
import com.gwz.crowdfunding.bean.Ticket;

/**
 * 服务访问类
 * @author DELL
 *
 */
@FeignClient("eureka-member-service")
public interface MemberService {

	@RequestMapping("/login/{loginacct}")
	public Member login(@PathVariable("loginacct") String loginacct);

	@RequestMapping("/queryTicketByMemberid/{id}")
	public Ticket queryTicketByMemberid(@PathVariable("id") Integer id);

	@RequestMapping("/insertTicket")
	public void insertTicket(@RequestBody Ticket t);

	@RequestMapping("/updateAccountType")
	public void updateAccountType(@RequestBody Member loginMember);

	@RequestMapping("/updateBasicInfo")
	public void updateBasicInfo(@RequestBody Member loginMember);

	@RequestMapping("/queryCertsByAccountType/{accttype}")
	public List<Cert> queryCertsByAccountType(@PathVariable("accttype") String accttype);

	@RequestMapping("/insertMemberCerts")
	public void insertMemberCerts(@RequestBody List<MemberCert> mcs);

	@RequestMapping("/updateEmail")
	public void updateEmail(@RequestBody Member loginMember);

	@RequestMapping("/updateTicketAuthcode")
	public void updateTicketAuthcode(@RequestBody Ticket t);
	
	@RequestMapping("/updateAuthstatus")
	public void updateAuthstatus(@RequestBody Member loginMember);
}
