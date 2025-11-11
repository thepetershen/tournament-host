package com.tournamenthost.connect.frontend.with.backend.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        log.info("Attempting to send password reset email to: {}", toEmail);
        log.debug("Reset token: {}", resetToken);
        log.debug("Frontend URL: {}", frontendUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password - Tournament Host");
            helper.setFrom("thepetershen@gmail.com"); // Must match verified sender in SendGrid

            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            log.debug("Reset link: {}", resetLink);

            String htmlContent = buildPasswordResetEmailHtml(resetLink);
            String textContent = buildPasswordResetEmailText(resetLink);

            helper.setText(textContent, htmlContent);

            log.info("Sending email via JavaMailSender...");
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("MessagingException while sending email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            log.error("Unexpected exception while sending email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildPasswordResetEmailText(String resetLink) {
        return """
                Password Reset Request

                Hi there,

                You requested to reset your password for your Tournament Host account.

                Click the link below to reset your password:
                %s

                SECURITY NOTICE:
                - This link will expire in 1 hour
                - If you didn't request this, you can safely ignore this email
                - Your password won't change until you create a new one

                Tournament Host Team
                This is an automated email, please do not reply.
                """.formatted(resetLink);
    }

    private String buildPasswordResetEmailHtml(String resetLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #4A90E2;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .content {
                            background-color: #f9f9f9;
                            padding: 30px;
                            border-radius: 0 0 5px 5px;
                        }
                        .button {
                            display: inline-block;
                            padding: 12px 30px;
                            background-color: #4A90E2;
                            color: white;
                            text-decoration: none;
                            border-radius: 5px;
                            margin: 20px 0;
                            font-weight: bold;
                        }
                        .footer {
                            margin-top: 20px;
                            font-size: 12px;
                            color: #666;
                            text-align: center;
                        }
                        .warning {
                            background-color: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 10px;
                            margin: 15px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>Password Reset Request</h1>
                    </div>
                    <div class="content">
                        <p>Hi there,</p>
                        <p>You requested to reset your password for your Tournament Host account.</p>
                        <p>Click the button below to reset your password:</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">Reset Password</a>
                        </div>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #4A90E2;">%s</p>
                        <div class="warning">
                            <strong>⚠️ Security Notice:</strong>
                            <ul>
                                <li>This link will expire in <strong>1 hour</strong></li>
                                <li>If you didn't request this, you can safely ignore this email</li>
                                <li>Your password won't change until you create a new one</li>
                            </ul>
                        </div>
                    </div>
                    <div class="footer">
                        <p>Tournament Host Team</p>
                        <p>This is an automated email, please do not reply.</p>
                    </div>
                </body>
                </html>
                """.formatted(resetLink, resetLink);
    }
}
