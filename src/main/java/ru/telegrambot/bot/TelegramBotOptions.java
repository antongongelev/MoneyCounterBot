package ru.telegrambot.bot;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Component
public class TelegramBotOptions {

    @Value("${domain.proxy.enabled:false}")
    private Boolean proxyEnabled;

    @Value("${domain.proxy.host:localhost}")
    private String proxyHost;

    @Value("${domain.proxy.port:8080}")
    private Integer proxyPort;

    public DefaultBotOptions buildOptions() {
        DefaultBotOptions botOptions = new DefaultBotOptions();

        if (BooleanUtils.isTrue(proxyEnabled)) {
            botOptions.setProxyHost(proxyHost);
            botOptions.setProxyPort(proxyPort);
            botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
        }

        return botOptions;
    }

}
