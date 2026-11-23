package com.puplopez.clms.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.puplopez.clms.model.Computer;
import com.puplopez.clms.model.MaintenanceLog;
import com.puplopez.clms.model.Report;
import com.puplopez.clms.model.User;
import com.puplopez.clms.repository.ComputerRepository;
import com.puplopez.clms.repository.LaboratoryRepository;
import com.puplopez.clms.repository.MaintenanceLogRepository;
import com.puplopez.clms.repository.ReportRepository;
import com.puplopez.clms.repository.UserRepository;

@Controller
public class DashboardController {

    private final LaboratoryRepository labRepo;
    private final ComputerRepository computerRepo;
    private final ReportRepository reportRepo;
    private final MaintenanceLogRepository logRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public DashboardController(LaboratoryRepository labRepo,
                               ComputerRepository computerRepo,
                               ReportRepository reportRepo,
                               MaintenanceLogRepository logRepo,
                               UserRepository userRepo,
                               PasswordEncoder passwordEncoder) {
        this.labRepo = labRepo;
        this.computerRepo = computerRepo;
        this.reportRepo = reportRepo;
        this.logRepo = logRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerProfessor(@RequestParam String fullName,
                                    @RequestParam String username,
                                    @RequestParam String password,
                                    @RequestParam String confirmPassword,
                                    RedirectAttributes redirectAttributes) {
        String cleanUsername = username.trim().toLowerCase();

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/register";
        }
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters.");
            return "redirect:/register";
        }
        if (userRepo.existsByUsername(cleanUsername)) {
            redirectAttributes.addFlashAttribute("error", "Username '" + cleanUsername + "' is already taken.");
            return "redirect:/register";
        }

        User prof = new User();
        prof.setFullName(fullName.trim());
        prof.setUsername(cleanUsername);
        prof.setPassword(passwordEncoder.encode(password));
        prof.setRole("PROFESSOR");
        userRepo.save(prof);

        redirectAttributes.addFlashAttribute("registered", "Account created! You can now sign in.");
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Integer labId,
                            Authentication auth,
                            Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("laboratories", labRepo.findAll());
        model.addAttribute("selectedLabId", labId);

        model.addAttribute("totalCount", computerRepo.count());
        model.addAttribute("workingCount", computerRepo.countByStatus("Working"));
        model.addAttribute("pendingCount", computerRepo.countByStatus("Pending"));
        model.addAttribute("maintenanceCount", computerRepo.countByStatus("Under Maintenance"));
        model.addAttribute("damagedCount", computerRepo.countByStatus("Damaged"));

        List<Report> reports = (labId != null)
                ? reportRepo.findByComputerLaboratoryLabIdOrderByReportDateDesc(labId)
                : reportRepo.findAllByOrderByReportDateDesc();

        model.addAttribute("reports", reports);
        model.addAttribute("logs", logRepo.findAllByOrderByMaintenanceDateDesc());
        model.addAttribute("users", userRepo.findAll());
        return "dashboard";
    }

    @PostMapping("/reports/submit")
    public String submitReport(@RequestParam Integer labId,
                               @RequestParam String computerNumber,
                               @RequestParam String problemDescription,
                               @RequestParam(required = false) String notes,
                               @RequestParam(required = false) MultipartFile attachment,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) throws IOException {

        Optional<Computer> compOpt = computerRepo.findByLaboratoryLabIdAndComputerNumberIgnoreCase(labId, computerNumber.trim());
        if (compOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Computer '" + computerNumber + "' not found in the selected laboratory.");
            return "redirect:/dashboard";
        }

        Computer computer = compOpt.get();
        computer.setStatus("Pending");
        computerRepo.save(computer);

        Report report = new Report();
        report.setComputer(computer);
        report.setReportedBy(auth.getName());
        report.setProblemDescription(problemDescription.trim());
        report.setNotes(notes != null && !notes.isBlank() ? notes.trim() : null);
        report.setStatus("Pending");

        if (attachment != null && !attachment.isEmpty()) {
            Path uploadDir = Paths.get("uploads");
            Files.createDirectories(uploadDir);
            String filename = UUID.randomUUID() + "_" + attachment.getOriginalFilename();
            Files.copy(attachment.getInputStream(), uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            report.setAttachmentPath("/uploads/" + filename);
            report.setAttachmentType(attachment.getContentType());
        }

        reportRepo.save(report);
        redirectAttributes.addFlashAttribute("success", "Report logged for " + computer.getComputerNumber() + " (" + computer.getLaboratory().getLabName() + ").");
        return "redirect:/dashboard";
    }

    @PostMapping("/coordinator/reports/{reportId}/update")
    public String updateReportStatus(@PathVariable Integer reportId,
                                     @RequestParam String newStatus,
                                     @RequestParam String actionTaken,
                                     @RequestParam(required = false) String resolutionDetails,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {

        Report report = reportRepo.findById(reportId).orElseThrow();
        report.setStatus(newStatus);
        reportRepo.save(report);

        Computer computer = report.getComputer();
        computer.setStatus("Resolved".equals(newStatus) ? "Working" : newStatus);
        computerRepo.save(computer);

        MaintenanceLog log = new MaintenanceLog();
        log.setReport(report);
        log.setCoordinatorName(auth.getName());
        log.setActionTaken(actionTaken.trim());
        log.setResolutionDetails(resolutionDetails != null ? resolutionDetails.trim() : null);
        logRepo.save(log);

        redirectAttributes.addFlashAttribute("success", "Updated Report #" + reportId + " to '" + newStatus + "' and recorded maintenance log.");
        return "redirect:/dashboard";
    }

    @PostMapping("/coordinator/users/create")
    public String createUser(@RequestParam String fullName,
                             @RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String role,
                             RedirectAttributes redirectAttributes) {
        String cleanUsername = username.trim().toLowerCase();
        if (userRepo.existsByUsername(cleanUsername)) {
            redirectAttributes.addFlashAttribute("error", "Username '" + cleanUsername + "' already exists.");
            return "redirect:/dashboard";
        }

        User newUser = new User();
        newUser.setFullName(fullName.trim());
        newUser.setUsername(cleanUsername);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(role);
        userRepo.save(newUser);

        redirectAttributes.addFlashAttribute("success", "Account created for " + fullName + " (" + cleanUsername + ").");
        return "redirect:/dashboard";
    }
}