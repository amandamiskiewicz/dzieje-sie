package com.example.event_backend.controller;

import com.example.event_backend.model.Event;
import com.example.event_backend.model.EventParticipant;
import com.example.event_backend.model.User;
import com.example.event_backend.repository.EventParticipantRepository;
import com.example.event_backend.repository.EventRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Stream;

@Controller
@RequestMapping("/events")
public class EventParticipantController {

    private final EventParticipantRepository participantRepository;
    private final EventRepository eventRepository;

    public EventParticipantController(
            EventParticipantRepository participantRepository,
            EventRepository eventRepository) {
        this.participantRepository = participantRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/{eventId}/participants")
    public String showParticipants(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono wydarzenia"));

        if (!event.getUser().getEmail().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("Tylko organizator może przeglądać uczestników");
        }

        List<EventParticipant> participants = participantRepository.findByEvent(event);
        model.addAttribute("event", event);
        model.addAttribute("participants", participants);
        
        return "event-participants";
    }

    @GetMapping("/{eventId}/participants/export/csv")
    public ResponseEntity<Resource> exportParticipantsToCSV(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono wydarzenia"));

        if (!event.getUser().getEmail().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("Tylko organizator może eksportować listę uczestników");
        }

        List<EventParticipant> participants = participantRepository.findByEvent(event);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        
        writer.println("Imię,Nazwisko,Email,Telefon");
        
        for (EventParticipant participant : participants) {
            User user = participant.getUser();
            writer.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\"",
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhone() != null ? user.getPhone() : ""));
        }
        
        writer.flush();
        writer.close();
        
        ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=uczestnicy_" + eventId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    @GetMapping("/{eventId}/participants/export/pdf")
    public ResponseEntity<Resource> exportParticipantsToPDF(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException, DocumentException {
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono wydarzenia"));

        if (!event.getUser().getEmail().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("Tylko organizator może eksportować listę uczestników");
        }

        List<EventParticipant> participants = participantRepository.findByEvent(event);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        
        BaseFont baseFont = BaseFont.createFont(
                BaseFont.HELVETICA, 
                BaseFont.CP1250, 
                BaseFont.EMBEDDED);
        
        Font polishFont = new Font(baseFont, 12);
        Font polishFontBold = new Font(baseFont, 12, Font.BOLD);
        Font titleFont = new Font(baseFont, 18, Font.BOLD);
        
        document.open();
        
        Paragraph title = new Paragraph("Lista uczestników wydarzenia", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);
        
        Paragraph eventInfo = new Paragraph(
                "Wydarzenie: " + event.getTitle(), 
                polishFont);
        eventInfo.setSpacingAfter(10f);
        document.add(eventInfo);
        
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
        String formattedDate = dateFormat.format(new java.util.Date());
        
        Paragraph dateInfo = new Paragraph(
                "Data wygenerowania: " + formattedDate,
                polishFont);
        dateInfo.setSpacingAfter(20f);
        document.add(dateInfo);
        
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
    
        Stream.of("Imię", "Nazwisko", "Email", "Telefon")
            .forEach(columnTitle -> {
                PdfPCell header = new PdfPCell(new Phrase(columnTitle, polishFontBold));
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header.setBorderWidth(2);
                table.addCell(header);
            });
        
        for (EventParticipant participant : participants) {
            User user = participant.getUser();
            table.addCell(new Phrase(user.getFirstName(), polishFont));
            table.addCell(new Phrase(user.getLastName(), polishFont));
            table.addCell(new Phrase(user.getEmail(), polishFont));
            table.addCell(new Phrase(user.getPhone() != null ? user.getPhone() : "Brak", polishFont));
        }

        document.add(table);
        document.close();
        
        ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=uczestnicy_" + eventId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @PostMapping("/{eventId}/participants/{participantId}/remove")
    public String removeParticipant(
            @PathVariable Long eventId,
            @PathVariable Long participantId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono wydarzenia"));

        if (!event.getUser().getEmail().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("Tylko organizator może usuwać uczestników");
        }

        try {
            participantRepository.deleteById(participantId);
            redirectAttributes.addFlashAttribute("success", "Usunięto uczestnika pomyślnie");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd podczas usuwania uczestnika");
        }
        
        return "redirect:/events/" + eventId + "/participants";
    }

    @PostMapping("/participants/{participantId}/cancel")
    public String cancelParticipation(
            @PathVariable Long participantId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        EventParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono uczestnictwa"));

        if (!participant.getUser().getEmail().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("Możesz anulować tylko własne uczestnictwo");
        }

        participantRepository.delete(participant);
        redirectAttributes.addFlashAttribute("success", "Anulowano uczestnictwo");
        
        return "redirect:/dashboard";
    }
}