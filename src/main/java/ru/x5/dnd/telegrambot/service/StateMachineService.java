package ru.x5.dnd.telegrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.x5.dnd.telegrambot.config.StateMachineEvents;
import ru.x5.dnd.telegrambot.config.StateMachineStates;

@Service
public class StateMachineService {

    private final static Logger log = LoggerFactory.getLogger(StateMachineService.class);

    private final StateMachineFactory<StateMachineStates, StateMachineEvents> stateMachineFactory;
    private final StateMachinePersister<StateMachineStates, StateMachineEvents, Object> stateMachinePersister;

    public StateMachineService(StateMachineFactory<StateMachineStates, StateMachineEvents> stateMachineFactory,
                               StateMachinePersister<StateMachineStates, StateMachineEvents, Object> stateMachinePersister) {
        this.stateMachineFactory = stateMachineFactory;
        this.stateMachinePersister = stateMachinePersister;
    }


    public void sendToStateMachine(String machineId, Message<StateMachineEvents> message) {
        var stateMachine = restoreStateMachine(machineId);

        var result = stateMachine.sendEvent(Mono.just(message))
                .blockFirst();
        log.debug("Transition result is {}. Machine state is {}", result, stateMachine.getState());

        persistStateMachine(stateMachine, machineId);
    }

    private void persistStateMachine(StateMachine<StateMachineStates, StateMachineEvents> stateMachine, String machineId) {
        try {
            stateMachinePersister.persist(stateMachine, machineId);
        } catch (Exception e) {
            log.error("Error persisting state machine for chat {}", machineId, e);
        }
    }

    private StateMachine<StateMachineStates, StateMachineEvents> restoreStateMachine(String machineId) {
        var stateMachine = stateMachineFactory.getStateMachine(machineId);
        try {
            stateMachinePersister.restore(stateMachine, machineId);
        } catch (Exception e) {
            log.error("Error restoring state machine for chat {}", machineId, e);
        }
        stateMachine.startReactively().block();
        return stateMachine;
    }

}
