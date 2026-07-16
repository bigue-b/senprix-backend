package sn.dci.senprix.prix.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Déclare la topologie RabbitMQ utilisée pour notifier alerte-service
 * lorsqu'un relevé de prix suspect est détecté. prix-service est ici
 * uniquement producteur : il publie sur l'exchange, à charge pour
 * alerte-service de router sa propre queue dessus.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_PRIX_SUSPECT = "prix.suspect.exchange";
    public static final String QUEUE_ALERTE = "alerte.queue";
    public static final String ROUTING_KEY_PRIX_SUSPECT = "prix.suspect";

    @Bean
    public DirectExchange prixSuspectExchange() {
        return new DirectExchange(EXCHANGE_PRIX_SUSPECT);
    }

    @Bean
    public Queue alerteQueue() {
        return new Queue(QUEUE_ALERTE, true);
    }

    @Bean
    public Binding alerteQueueBinding(Queue alerteQueue, DirectExchange prixSuspectExchange) {
        return BindingBuilder.bind(alerteQueue).to(prixSuspectExchange).with(ROUTING_KEY_PRIX_SUSPECT);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }
}
