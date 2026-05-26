package com.kluniversity.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.kluniversity.entity.HostelBooking;
import com.kluniversity.entity.Payment;
import com.kluniversity.entity.Student;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class PdfGenerator {
    public byte[] feeReceipt(Payment payment) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();
        addTitle(doc, "KL University Fee Receipt");
        Student student = payment.getStudent();
        addLine(doc, "Register No: " + student.getRegNo());
        addLine(doc, "Student: " + student.getFirstName() + " " + student.getLastName());
        addLine(doc, "Department: " + student.getDepartment());
        addLine(doc, "Semester: " + payment.getSemester());
        addLine(doc, "Amount Paid: Rs. " + payment.getAmount());
        addLine(doc, "Transaction ID: " + payment.getTransactionId());
        addLine(doc, "Payment Method: " + payment.getPaymentMethod());
        addLine(doc, "Payment Date: " + payment.getPaymentDate());
        doc.close();
        return out.toByteArray();
    }

    public byte[] hostelReceipt(HostelBooking booking) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();
        addTitle(doc, "KL University Hostel Booking Receipt");
        addLine(doc, "Register No: " + booking.getStudent().getRegNo());
        addLine(doc, "Student: " + booking.getStudent().getFirstName() + " " + booking.getStudent().getLastName());
        addLine(doc, "Hostel: " + booking.getHostel().getHostelName());
        addLine(doc, "Room Number: " + booking.getRoomNumber());
        addLine(doc, "Room Type: " + booking.getRoom().getRoomType());
        addLine(doc, "Mess Type: " + booking.getMessType());
        addLine(doc, "Room Fee: Rs. " + booking.getRoom().getRoomFee());
        addLine(doc, "Booked At: " + booking.getBookedAt());
        doc.close();
        return out.toByteArray();
    }

    public byte[] idCard(Student student, HostelBooking booking) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(new Rectangle(360, 520));
        PdfWriter.getInstance(doc, out);
        doc.open();
        addTitle(doc, "KL UNIVERSITY");
        addLine(doc, "Student ID Card");
        addLine(doc, "Name: " + student.getFirstName() + " " + student.getLastName());
        addLine(doc, "Register No: " + student.getRegNo());
        addLine(doc, "Department: " + student.getDepartment());
        addLine(doc, "Course: " + (student.getCourse() == null ? "N/A" : student.getCourse().getCourseName()));
        addLine(doc, "Hostel: " + (booking == null ? "Not allocated" : booking.getHostel().getHostelName()));
        addLine(doc, "Room: " + (booking == null ? "Not allocated" : booking.getRoomNumber()));
        try {
            Image qr = Image.getInstance(qrBytes(student.getRegNo() + "|" + student.getEmail()));
            qr.scaleToFit(100, 100);
            doc.add(qr);
        } catch (Exception ignored) {
            addLine(doc, "QR: " + student.getRegNo());
        }
        doc.close();
        return out.toByteArray();
    }

    private byte[] qrBytes(String value) throws Exception {
        BitMatrix matrix = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 180, 180);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", png);
        return png.toByteArray();
    }

    private void addTitle(Document doc, String text) {
        try {
            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph p = new Paragraph(text, font);
            p.setSpacingAfter(16);
            doc.add(p);
        } catch (DocumentException ignored) {
        }
    }

    private void addLine(Document doc, String text) {
        try {
            Paragraph p = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 11));
            p.setSpacingAfter(8);
            doc.add(p);
        } catch (DocumentException ignored) {
        }
    }
}
