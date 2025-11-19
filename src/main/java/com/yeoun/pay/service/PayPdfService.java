package com.yeoun.pay.service;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.yeoun.pay.dto.EmpPayslipResponseDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayPdfService {

    private final SpringTemplateEngine templateEngine;
    private final PayslipDetailService detailService; 
    
    public byte[] generatePdf(Long payslipId) {

        // ① 완성된 DTO (header + items)
        EmpPayslipResponseDTO dto = detailService.getDetail(payslipId);

        // ② DTO 통째로 전달
        Context context = new Context();
        context.setVariable("slip", dto.getHeader());   // 기본정보
        context.setVariable("items", dto.getItems());   // 항목목록
        context.setVariable("payYymmFormatted", dto.getPayYymmFormatted());  // 🔥 여기!!


        String html = templateEngine.process("pay/payslip_pdf", context);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();

            String baseUrl = new ClassPathResource("static/").getURL().toString();
            builder.withHtmlContent(html, baseUrl);

            String fontPath = new ClassPathResource("static/assets/vendor/fonts/NotoSansKR.ttf")
                    .getFile().getPath();
            builder.useFont(new File(fontPath), "NotoSansKR");

            builder.useFastMode();
            builder.toStream(out);
            builder.run();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF 생성 실패: " + e.getMessage(), e);
        }
    }
}
