package org.mifos.connector.notification.template;

import org.apache.camel.Exchange;
import org.mifos.connector.notification.config.properties.VelocityProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

import static org.mifos.connector.notification.camel.config.CamelProperties.*;

@Component
public class TemplateDefaultConfig {


    private final VelocityProperties velocity;

    public TemplateDefaultConfig(VelocityProperties velocity) {
        this.velocity = velocity;
    }

    public static String nvlTransactionId(String value, String alternateValue) {
        if (value.equals("null"))
            return alternateValue;
        else
            return value;
    }

    public static String nvlAccount(String value, String alternateValue) {
        if (value.equals("null"))
            return alternateValue;
        else {
            return value.replaceAll("\\d(?=(?:\\D*\\d){4})", "*");
        }

    }

    public static String nvlAmount(String value, String alternateValue) {
        if (value.equals("null"))
            return alternateValue;
        else
            return value;
    }

    public static String nvlDate(String value, String alternateValue) {
        if (value.equals("null"))
            return String.valueOf(new Date().getTime());
        else
            return value;
    }

    public static String nvlCurrency(String value, String alternateValue) {
        if (value.equals("null"))
            return alternateValue;
        else
            return value;
    }

    public static String nvlTxnType(String value, String alternateValue) {
        if (value.equals("null"))
            return alternateValue;
        else
            return value;
    }

    public static String nvlFailType(String value, String alternateValue) {
        if (value.equals("null"))
            return alternateValue;
        else
            return value;
    }

    public TemplateConfig replaceTemplatePlaceholders(TemplateConfig templateConfig, Exchange exchange) {

        templateConfig.getVelocityContext().put(velocity.transactionid(), nvlTransactionId(
                String.valueOf(exchange.getProperty(CORRELATION_ID)), velocity.defaults().transactionid()));
        templateConfig.getVelocityContext().put(velocity.amount(), nvlAmount(
                String.valueOf(exchange.getProperty(TRANSACTION_AMOUNT)), velocity.defaults().amount()));
        templateConfig.getVelocityContext().put(velocity.date(), nvlDate(
                String.valueOf(exchange.getProperty(DATE)), String.valueOf(new Date().getTime())));
        templateConfig.getVelocityContext().put(velocity.account(), nvlAccount(
                String.valueOf(exchange.getProperty(ACCOUNT_ID)), velocity.defaults().account()));
        templateConfig.getVelocityContext().put(velocity.currency(), nvlCurrency
                (String.valueOf(exchange.getProperty(CURRENCY)), velocity.defaults().currency()));
        templateConfig.getVelocityContext().put(velocity.txnType(), nvlTxnType
                (String.valueOf(exchange.getProperty(TRANSACTION_TYPE)), velocity.defaults().txnType()));
        templateConfig.getVelocityContext().put(velocity.failureType(), nvlFailType
                (String.valueOf(exchange.getProperty(ERROR_DESCRIPTION)), velocity.defaults().failureType()));
        return templateConfig;
    }
}
