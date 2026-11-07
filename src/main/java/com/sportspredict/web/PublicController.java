package com.sportspredict.web;

import com.sportspredict.model.AnalyticsEvent.Type;
import com.sportspredict.model.Inquiry;
import com.sportspredict.model.Match;
import com.sportspredict.model.Subscriber;
import com.sportspredict.repo.*;
import com.sportspredict.service.AnalyticsService;
import com.sportspredict.service.MatchView;
import com.sportspredict.service.PredictionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Controller
public class PublicController {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final MatchRepository matches;
    private final TipRepository tips;
    private final InquiryRepository inquiries;
    private final SubscriberRepository subscribers;
    private final PredictionService predictions;
    private final AnalyticsService analytics;

    public PublicController(MatchRepository matches, TipRepository tips, InquiryRepository inquiries,
                            SubscriberRepository subscribers, PredictionService predictions,
                            AnalyticsService analytics) {
        this.matches = matches;
        this.tips = tips;
        this.inquiries = inquiries;
        this.subscribers = subscribers;
        this.predictions = predictions;
        this.analytics = analytics;
    }

    /** Visitors may only fill in these fields of an inquiry. */
    @InitBinder("inquiry")
    void restrictInquiryFields(WebDataBinder binder) {
        binder.setAllowedFields("name", "email", "message");
    }

    @GetMapping("/")
    public String home(Model model) {
        List<MatchView> views = matches.findByPublishedTrueOrderByMatchDateAsc().stream()
                .map(m -> new MatchView(m, predictions.predict(m))).toList();
        model.addAttribute("matches", views);
        model.addAttribute("tips", tips.findByActiveTrueOrderByIdAsc());
        return "index";
    }

    @GetMapping("/match/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Match m = published(id);
        model.addAttribute("view", new MatchView(m, predictions.predict(m)));
        if (!model.containsAttribute("inquiry")) model.addAttribute("inquiry", new Inquiry());
        return "match";
    }

    @PostMapping("/match/{id}/vote")
    @Transactional
    public String vote(@PathVariable Long id, @RequestParam String side,
                       @RequestParam(defaultValue = "home") String back,
                       HttpSession session, RedirectAttributes ra) {
        Match m = published(id);
        @SuppressWarnings("unchecked")
        Set<Long> voted = (Set<Long>) session.getAttribute("voted");
        if (voted == null) voted = new HashSet<>();
        if (voted.contains(id)) {
            ra.addFlashAttribute("msg", "You have already shown your support for this match.");
        } else {
            if ("A".equals(side)) matches.addSupportA(id);
            else if ("B".equals(side)) matches.addSupportB(id);
            else throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            voted.add(id);
            session.setAttribute("voted", voted);
            analytics.record(Type.VOTE, "/match/" + id, id);
            ra.addFlashAttribute("msg", "Thanks! You are supporting "
                    + ("A".equals(side) ? m.getTeamA() : m.getTeamB()) + ". Odds updated.");
        }
        return "detail".equals(back) ? "redirect:/match/" + id : "redirect:/#match-" + id;
    }

    /** Fired by the "Check which team succeeds" button (via fetch) just for analytics. */
    @PostMapping("/api/match/{id}/check")
    @ResponseBody
    public ResponseEntity<Void> check(@PathVariable Long id) {
        analytics.record(Type.PREDICTION_CHECK, "/", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/match/{id}/inquiry")
    public String inquiry(@PathVariable Long id, @Valid @ModelAttribute("inquiry") Inquiry inquiry,
                          BindingResult result, Model model, RedirectAttributes ra) {
        Match m = published(id);
        if (result.hasErrors()) {
            model.addAttribute("view", new MatchView(m, predictions.predict(m)));
            return "match";
        }
        inquiry.setId(null);
        inquiry.setMatchId(id);
        inquiry.setMatchTitle(m.getTitle());
        inquiries.save(inquiry);
        analytics.record(Type.INQUIRY, "/match/" + id, id);
        ra.addFlashAttribute("msg", "Your question was sent to the admin. Thank you!");
        return "redirect:/match/" + id;
    }

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String email, RedirectAttributes ra) {
        String e = email == null ? "" : email.trim();
        if (!EMAIL.matcher(e).matches()) {
            ra.addFlashAttribute("msg", "Please enter a valid email address.");
        } else if (subscribers.existsByEmailIgnoreCase(e)) {
            ra.addFlashAttribute("msg", "You are already subscribed.");
        } else {
            subscribers.save(new Subscriber(e));
            analytics.record(Type.SUBSCRIBE, "/subscribe", null);
            ra.addFlashAttribute("msg", "Subscribed! We will email you new match details.");
        }
        return "redirect:/#subscribe";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    private Match published(Long id) {
        return matches.findById(id).filter(Match::isPublished)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
