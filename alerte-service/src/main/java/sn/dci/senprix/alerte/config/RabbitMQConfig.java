package sn.dci.senprix.alerte.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Déclare la même topologie RabbitMQ que prix-service (exchange, queue,
 * binding) afin que alerte-service puisse démarrer indépendamment de
 * l'ordre de lancement des deux services — la déclaration de topologie
 * Spring AMQP est idempotente, les deux services peuvent la déclarer
 * chacun de leur côté sans conflit.
 *
 * Le converter est configuré en TypePrecedence.INFERRED : le type Java
 * cible du message est déduit de la signature du @RabbitListener plutôt
 * que de l'en-tête __TypeId__ envoyé par prix-service (qui référence une
 * classe appartenant à un autre module, donc introuvable ici).
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
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }
}
