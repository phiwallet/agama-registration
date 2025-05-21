package org.gluu.agama;

import java.time.*;
import java.time.format.DateTimeFormatter;
import io.jans.agama.engine.service.LabelsService;
import io.jans.service.cdi.util.CdiUtil;


class EmailTemplate {
    
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, YYYY, HH:mma (O)");

    static String get(String otp) {
    LabelsService lbls = CdiUtil.bean(LabelsService);
    String templateMsgOne = lbls.get("mail.templateMsgOne");
    String templateMsgTwo = lbls.get("mail.templateMsgTwo");
    String templateMsgThree = lbls.get("mail.templateMsgThree");
    String templateMsgFour = lbls.get("mail.templateMsgFour");        

        """

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Phi Wallet Email</title>
  <style>
    @import url("https://fonts.googleapis.com/css?family=Nunito+Sans:400,700&display=swap");

    body, p {
      font-family: 'Nunito Sans', sans-serif;
    }
  </style>
</head>
<body style="margin: 0; padding: 0; background-color: #f9f9f9;">
  <div style="width: 640px; font-size: 18px; font-family: 'Nunito Sans', sans-serif; font-weight: 400; margin: 0 auto; background-color: #ffffff;">
    <!-- Logo Banner -->
    <div style="text-align: center; padding: 20px 0;">
      <img src="https://storage.googleapis.com/email_template_staticfiles/Phi_logo320x132_Aug2024.png"
           alt="Phi Wallet"
           width="160"
           height="66"
           style="display: block; margin: 0 auto;" />
    </div>

    <!-- Email Body -->
    <div style="padding: 20px; border-top: 1px solid #ccc; border-bottom: 1px solid #ccc;">
      <p style="margin-bottom: 16px;">
        <strong>Hi,</strong><br />
        ${templateMsgOne}
      </p>

      <!-- OTP Code -->
      <div style="text-align: center; margin: 30px 0;">
        <div style="
          display: inline-block;
          background-color: #e2e2e2;
          color: #AD9269;
          font-size: 36px;
          font-weight: 700;
          letter-spacing: 6px;
          padding: 12px 24px;
          border-radius: 8px;">
          ${otp}
        </div>
      </div>

      <p style="margin-bottom: 16px;">
        ${templateMsgTwo}
      </p>

      <p style="margin-bottom: 0;">
        ${templateMsgThree}<br />
        ${templateMsgFour}
      </p>
    </div>
  </div>
</body>
</html>


        """
    }

    private static String computeDateTime(String zone) {

        Instant now = Instant.now();
        try {
            return now.atZone(ZoneId.of(zone)).format(formatter);
        } catch (Exception e) {
            return now.atOffset(ZoneOffset.UTC).format(formatter);
        }
        
    }
    
}
