package com.sportspredict.config;

import com.sportspredict.model.AnalyticsEvent;
import com.sportspredict.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Counts public page views for the admin analytics panel. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AnalyticsService analytics;

    public WebConfig(AnalyticsService analytics) {
        this.analytics = analytics;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void afterCompletion(HttpServletRequest req, HttpServletResponse res,
                                        Object handler, Exception ex) {
                if (!"GET".equals(req.getMethod()) || res.getStatus() != 200) return;
                String path = req.getRequestURI();
                Long matchId = null;
                if (path.startsWith("/match/")) {
                    try {
                        matchId = Long.parseLong(path.substring("/match/".length()));
                    } catch (NumberFormatException ignored) { }
                }
                analytics.record(AnalyticsEvent.Type.PAGE_VIEW, path, matchId);
            }
        }).addPathPatterns("/", "/match/*", "/faq");
    }
}
