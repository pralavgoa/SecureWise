package edu.ucla.wise.admin.security;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.SanityCheck;

@WebFilter("/admin/*")
public class RequestParametersCheckerFilter implements Filter{

	private static final Logger LOGGER = Logger.getLogger(RequestParametersCheckerFilter.class);
	
	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		LOGGER.info("Checking the sanity of the provided request parameters");
		
		
		
		Map<String, String[]> requestParametersMap = request.getParameterMap();
		for(String parameterName : requestParametersMap.keySet()){
			for(String parameterValue : requestParametersMap.get(parameterName)){
				if(SanityCheck.sanityCheck(parameterValue)){
					//Not a sanitized value
					HttpServletRequest httpServletRequest = (HttpServletRequest) request;
					HttpServletResponse httpServletResponse = (HttpServletResponse) response;
					httpServletResponse.sendRedirect( httpServletRequest.getContextPath()+ "/admin/sanity_error.html");
					return;
				}
			}
		}
		filterChain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}

}
