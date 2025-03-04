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
    private final Action<StateMachineStates, StateMachineEvents> greetMembersAction;
    private final Action<StateMachineStates, StateMachineEvents> searchInfoAction;
    private final Action<StateMachineStates, StateMachineEvents> announceAction;
    private final Action<StateMachineStates, StateMachineEvents> announceCallbackAction;
    private final Action<StateMachineStates, StateMachineEvents> statsAction;

    public StateMachineConfiguration(Action<StateMachineStates, StateMachineEvents> echoAction,
                                     Action<StateMachineStates, StateMachineEvents> exceptionHandlerAction,
                                     Action<StateMachineStates, StateMachineEvents> greetMembersAction,
                                     Action<StateMachineStates, StateMachineEvents> announceAction,
                                     Action<StateMachineStates, StateMachineEvents> announceCallbackAction,
                                     Action<StateMachineStates, StateMachineEvents> statsAction,
                                     Action<StateMachineStates, StateMachineEvents> searchInfoAction) {
        this.echoAction = echoAction;
        this.exceptionHandlerAction = exceptionHandlerAction;
        this.greetMembersAction = greetMembersAction;
        this.searchInfoAction = searchInfoAction;

        this.announceAction = announceAction;
        this.announceCallbackAction = announceCallbackAction;
        this.statsAction = statsAction;
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
                .event(StateMachineEvents.COMMAND_ECHO)
                .action(echoAction, exceptionHandlerAction)
                .and()
                .withExternal()
                .source(StateMachineStates.ECHO).target(StateMachineStates.READY)
                .and()
        ;

        // GREET_NEW_MEMBERS
        addStateFlow(transitions, StateMachineStates.GREET_NEW_MEMBERS, StateMachineEvents.NEW_MEMBERS, greetMembersAction);

        // COMMAND_STATS
        addStateFlow(transitions, StateMachineStates.GET_GAME_STATS, StateMachineEvents.COMMAND_STATS, statsAction);

        // ANNOUNCE_GAME
        addStateFlow(transitions, StateMachineStates.ANNOUNCE_GAME, StateMachineEvents.COMMAND_ANNOUNCE, announceAction);
        // ANNOUNCE_CALLBACK
        addStateFlow(transitions, StateMachineStates.ANNOUNCE_CALLBACK, StateMachineEvents.CALLBACK_ANNOUNCE, announceCallbackAction);

        // COMMAND_SEARCH_INFO
        addStateFlow(transitions, StateMachineStates.HELP, StateMachineEvents.COMMAND_HELP, searchInfoAction);
        // COMMAND_SEARCH_CALLBACK
        addStateFlow(transitions, StateMachineStates.HELP_CALLBACK, StateMachineEvents.CALLBACK_HELP, searchInfoAction);
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

    /**
     * Добавление нового состояния в поток обработки машины состояний
     *
     * @param transitions транзит состояний
     * @param state статус состояние
     * @param stateEvent статус события состояния
     * @param action действие на событие
     * @throws Exception ошибка конфигурации
     */
    private void addStateFlow(final StateMachineTransitionConfigurer<StateMachineStates, StateMachineEvents> transitions,
                              final StateMachineStates state,
                              final StateMachineEvents stateEvent,
                              final Action<StateMachineStates, StateMachineEvents> action) throws Exception {
        transitions.withExternal()
                .source(StateMachineStates.READY).target(state)
                .event(stateEvent)
                .action(action, exceptionHandlerAction)
                .and()
                .withExternal()
                .source(state).target(StateMachineStates.READY)
                .and();
    }
}
