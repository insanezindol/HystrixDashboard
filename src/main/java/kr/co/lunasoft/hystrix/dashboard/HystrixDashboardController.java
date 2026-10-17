package kr.co.lunasoft.hystrix.dashboard;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Hystrix Dashboard 화면 컨트롤러.
 */
@Controller
public class HystrixDashboardController {

	@GetMapping("/hystrix")
	public String home(Model model, HttpServletRequest request) {
		addPaths(model, request);
		return "hystrix/index";
	}

	@GetMapping("/hystrix/monitor")
	public String monitor(Model model, HttpServletRequest request) {
		addPaths(model, request);
		return "hystrix/monitor";
	}

	private void addPaths(Model model, HttpServletRequest request) {
		String contextPath = request.getContextPath();
		model.addAttribute("contextPath", contextPath);
		model.addAttribute("basePath", contextPath + "/hystrix/");
	}

}
