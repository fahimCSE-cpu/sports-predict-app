package com.sportspredict.web;

import com.sportspredict.model.AnalyticsEvent.Type;
import com.sportspredict.model.Inquiry;
import com.sportspredict.model.Match;
import com.sportspredict.model.Subscriber;
import com.sportspredict.model.Tip;
import com.sportspredict.repo.*;
import com.sportspredict.service.AnalyticsService;
import com.sportspredict.service.MailService;
import com.sportspredict.service.MatchView;
import com.sportspredict.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final MatchRepository matches;
    private final InquiryRepository inquiries;
    private final SubscriberRepository subscribers;
    private final TipRepository tips;
    private final AnalyticsService analytics;
    private final PredictionService predictions;
    private final MailService mail;

    public AdminController(MatchRepository matches, InquiryRepository inquiries, SubscriberRepository subscribers,
                           TipRepository tips, AnalyticsService analytics, PredictionService predictions,
                           MailService mail) {
        this.matches = matches;
        this.inquiries = inquiries;
        this.subscribers = subscribers;
        this.tips = tips;
        this.analytics = analytics;
        this.predictions = predictions;
        this.mail = mail;
    }

    // ---------- Dashboard ----------
    @GetMapping
    public String dashboard(Model model) {
        List<Match> all = matches.findAllByOrderByMatchDateAsc();
        model.addAttribute("matchCount", all.size());
        model.addAttribute("publishedCount", all.stream().filter(Match::isPublished).count());
        model.addAttribute("openInquiries", inquiries.countByResolvedFalse());
        model.addAttribute("subscriberCount", subscribers.count());
        model.addAttribute("views", analytics.total(Type.PAGE_VIEW));
        model.addAttribute("checks", analytics.total(Type.PREDICTION_CHECK));
        model.addAttribute("votes", analytics.total(Type.VOTE));
        model.addAttribute("perDay", analytics.viewsPerDay(7));
        model.addAttribute("recent", inquiries.findTop5ByResolvedFalseOrderByCreatedAtDesc());
        model.addAttribute("section", "dashboard");
        return "admin/dashboard";
    }

    // ---------- Matches ----------
    @GetMapping("/matches")
    public String matchList(Model model) {
        model.addAttribute("matches", matches.findAllByOrderByMatchDateAsc().stream()
                .map(m -> new MatchView(m, predictions.predict(m))).toList());
        model.addAttribute("section", "matches");
        return "admin/matches";
    }

    @GetMapping("/matches/new")
    public String newMatch(Model model) {
        Match m = new Match();
        m.setSport("T20 Cricket");
        m.setPublished(true);
        model.addAttribute("match", m);
        model.addAttribute("section", "matches");
        return "admin/match-form";
    }

    @GetMapping("/matches/{id}/edit")
    public String editMatch(@PathVariable Long id, Model model) {
        model.addAttribute("match", matches.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        model.addAttribute("section", "matches");
        return "admin/match-form";
    }

    @PostMapping("/matches")
    public String saveMatch(@Valid @ModelAttribute("match") Match match, BindingResult result,
                            @RequestParam(defaultValue = "false") boolean notify,
                            Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("section", "matches");
            return "admin/match-form";
        }
        if (match.getId() != null) {
            matches.findById(match.getId()).ifPresent(old -> match.setCreatedAt(old.getCreatedAt()));
        }
        Match saved = matches.save(match);
        String msg = "Match saved" + (saved.isPublished() ? " and published to the front page." : " (hidden - not published).");
        if (notify && saved.isPublished()) {
            int n = mail.send(subscribers.findAll().stream().map(Subscriber::getEmail).toList(),
                    "New match: " + saved.getTitle(), matchMail(saved));
            msg += " Notified " + n + " subscriber(s)" + (mail.isLive() ? "." : " (mail disabled - logged only).");
        }
        ra.addFlashAttribute("msg", msg);
        return "redirect:/admin/matches";
    }

    @PostMapping("/matches/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        Match m = matches.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        m.setPublished(!m.isPublished());
        matches.save(m);
        ra.addFlashAttribute("msg", m.getTitle() + (m.isPublished() ? " is now published." : " is now hidden."));
        return "redirect:/admin/matches";
    }

    @PostMapping("/matches/{id}/delete")
    public String deleteMatch(@PathVariable Long id, RedirectAttributes ra) {
        matches.deleteById(id);
        ra.addFlashAttribute("msg", "Match deleted.");
        return "redirect:/admin/matches";
    }

    // ---------- Inquiries ----------
    @GetMapping("/inquiries")
    public String inquiryList(Model model) {
        model.addAttribute("inquiries", inquiries.findAllByOrderByCreatedAtDesc());
        model.addAttribute("section", "inquiries");
        return "admin/inquiries";
    }

    @PostMapping("/inquiries/{id}/reply")
    public String reply(@PathVariable Long id, @RequestParam String reply, RedirectAttributes ra) {
        Inquiry q = inquiries.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        q.setReply(reply.trim());
        q.setResolved(true);
        inquiries.save(q);
        mail.send(List.of(q.getEmail()), "Re: your question about " + q.getMatchTitle(),
                "Hi " + q.getName() + ",\n\n" + q.getReply() + "\n\n- Sports Predictive Selection System");
        ra.addFlashAttribute("msg", "Reply saved" + (mail.isLive() ? " and emailed." : " (email disabled - logged only)."));
        return "redirect:/admin/inquiries";
    }

    @PostMapping("/inquiries/{id}/delete")
    public String deleteInquiry(@PathVariable Long id) {
        inquiries.deleteById(id);
        return "redirect:/admin/inquiries";
    }

    // ---------- Analytics ----------
    @GetMapping("/analytics")
    public String analyticsPage(Model model) {
        model.addAttribute("perDay", analytics.viewsPerDay(7));
        model.addAttribute("perMatch", analytics.perMatch());
        model.addAttribute("views", analytics.total(Type.PAGE_VIEW));
        model.addAttribute("checks", analytics.total(Type.PREDICTION_CHECK));
        model.addAttribute("votes", analytics.total(Type.VOTE));
        model.addAttribute("inquiryCount", analytics.total(Type.INQUIRY));
        model.addAttribute("subs", subscribers.count());
        model.addAttribute("section", "analytics");
        return "admin/analytics";
    }

    // ---------- Mail ----------
    @GetMapping("/mail")
    public String mailPage(Model model) {
        model.addAttribute("subscribers", subscribers.findAll());
        model.addAttribute("live", mail.isLive());
        model.addAttribute("section", "mail");
        return "admin/mail";
    }

    @PostMapping("/mail")
    public String sendMail(@RequestParam String subject, @RequestParam String body, RedirectAttributes ra) {
        if (subject.isBlank() || body.isBlank()) {
            ra.addFlashAttribute("msg", "Subject and message are required.");
        } else {
            int n = mail.send(subscribers.findAll().stream().map(Subscriber::getEmail).toList(), subject, body);
            ra.addFlashAttribute("msg", "Update sent to " + n + " subscriber(s)"
                    + (mail.isLive() ? "." : " (mail disabled - written to the log only)."));
        }
        return "redirect:/admin/mail";
    }

    // ---------- Tips & offers ----------
    @GetMapping("/tips")
    public String tipList(Model model) {
        model.addAttribute("tips", tips.findAll());
        model.addAttribute("section", "tips");
        return "admin/tips";
    }

    @PostMapping("/tips")
    public String addTip(@RequestParam String title, @RequestParam String body,
                         @RequestParam Tip.Kind kind, RedirectAttributes ra) {
        if (title.isBlank() || body.isBlank()) {
            ra.addFlashAttribute("msg", "Title and text are required.");
        } else {
            tips.save(new Tip(title.trim(), body.trim(), kind));
            ra.addFlashAttribute("msg", "Added.");
        }
        return "redirect:/admin/tips";
    }

    @PostMapping("/tips/{id}/delete")
    public String deleteTip(@PathVariable Long id) {
        tips.deleteById(id);
        return "redirect:/admin/tips";
    }

    private String matchMail(Match m) {
        var p = predictions.predict(m);
        return String.format("%s (%s)%nDate: %s%nVenue: %s%n%nPrediction: %s %.1f%% vs %s %.1f%%%n%n%s%n",
                m.getTitle(), m.getSport(), m.getMatchDate(), m.getVenue() == null ? "TBA" : m.getVenue(),
                m.getTeamA(), p.pctA(), m.getTeamB(), p.pctB(), m.getDetails() == null ? "" : m.getDetails());
    }
}
