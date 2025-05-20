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
<div style="width: 640px; font-size: 18px; font-family: 'Roboto', sans-serif; font-weight: 300">
    <div style="background-color: #b6f6da; border-bottom: 1px solid #0ca65d">
        <img src="https://gluu.org/wp-content/uploads/elementor/thumbs/Logo-qbe8p4qgmufqni0becxda6fnfib6krzb65uihag270.png" alt="Gluu Inc." />
    </div>
    <div style="padding: 12px; border-bottom: 1px solid #ccc;">
        <p>
        <b>Hi,</b>
        <br><br>
        ${templateMsgOne} 
        </p>
        <div style="display: flex; justify-content: center">
            <div style="background-color: #b6f6da; color: #0ca65d; font-size: 40px; font-weight: 400; letter-spacing: 6px" align="center">
                ${otp}
            </div>
        </div>
        <p style="font-size: 14px">
        ${templateMsgTwo}
        </p>
        <p>
        <br>
        ${templateMsgThree}<br> 
        ${templateMsgFour}
        <br><br>
        </p>
    </div>
</div>
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
