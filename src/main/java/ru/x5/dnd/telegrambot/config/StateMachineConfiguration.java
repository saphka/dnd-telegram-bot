package ru.x5.dnd.telegrambot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfiguration extends EnumStateMachineConfigurerAdapter<StateMachineStates, StateMachineEvents> {

    private final Action<StateMachineStates, StateMachineEvents> echoAction;
    private final Action<StateMachineStates, StateMachineEvents> exceptionHandlerAction;

    public StateMachineConfiguration(Action<StateMachineStates, StateMachineEvents> echoAction,
                                     Action<StateMachineStates, StateMachineEvents> exceptionHandlerAction) {
        this.echoAction = echoAction;
        this.exceptionHandlerAction = exceptionHandlerAction;
    }

    @Override
    public void configure(StateMachineStateConfigurer<StateMachineStates, StateMachineEvents> states) throws Exception {
        states.withStates()
                .initial(StateMachineStates.READY)
                .states(EnumSet.allOf(StateMachineStates.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<StateMachineStates, StateMachineEvents> transitions) throws Exception {
        transitions.withExternal()
                .source(StateMachineStates.READY).target(StateMachineStates.ECHO)
                .event(StateMachineEvents.TEXT_INPUT)
                .action(echoAction, exceptionHandlerAction)
                .and()
                .withExternal()
                .source(StateMachineStates.ECHO).target(StateMachineStates.READY);
    }

    @Bean
    public StateMachinePersist<StateMachineStates, StateMachineEvents, Object> stateMachinePersist(JpaStateMachineRepository repository) {
        return new FixedJpaRepositoryStateMachinePersist<>(repository);
    }

    @Bean
    public StateMachinePersister<StateMachineStates, StateMachineEvents, Object> redisStateMachinePersister(
            StateMachinePersist<StateMachineStates, StateMachineEvents, Object> stateMachinePersist) {
        return new DefaultStateMachinePersister<>(stateMachinePersist);
    }
}
