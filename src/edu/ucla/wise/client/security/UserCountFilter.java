package edu.ucla.wise.client.security;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
/**
 *  Check if user hit counter indicates heavy traffic 
 *
 * @author pdessai
 *
 */
@WebFilter("/survey/*")
public class UserCountFilter implements Filter{

	private AtomicInteger userCounter;
	
	public static final int MAX_USERS_ALLOWED = 100000;
	
	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		userCounter.getAndIncrement();
		if(userCounter.get() > MAX_USERS_ALLOWED){
			response.getWriter().println("Too many users in the system"
				    + "<p> WISE Begin failed </p>"
				    + edu.ucla.wise.commons.SurveyorApplication.initErrorHtmlFoot);
			    return;
		}else{
			filterChain.doFilter(request, response);
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		userCounter = new AtomicInteger(0);
	}

}
