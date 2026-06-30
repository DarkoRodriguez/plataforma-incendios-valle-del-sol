package com.valledelsol.alerts;
import io.sentry.Sentry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MsAlertsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsAlertsApplication.class, args);
        Sentry.init(options -> {
            options.setDsn("https://372b3e52c2094891a1a698db8e96247b@app.glitchtip.com/25287");
            options.setTracesSampleRate(0.01); // 1% of transactions
        });

        // Verify your setup
        try {
            throw new Exception("Test GlitchTip error!");
        } catch (Exception e) {
            Sentry.captureException(e);
        }

        Sentry.flush(2000);
    }
}
