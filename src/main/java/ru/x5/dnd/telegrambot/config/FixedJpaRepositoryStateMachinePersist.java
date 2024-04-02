package ru.x5.dnd.telegrambot.config;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.data.jpa.JpaRepositoryStateMachine;
import org.springframework.statemachine.data.jpa.JpaRepositoryStateMachinePersist;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;

public class FixedJpaRepositoryStateMachinePersist<S, E> extends JpaRepositoryStateMachinePersist<S, E> {
    public FixedJpaRepositoryStateMachinePersist(JpaStateMachineRepository jpaStateMachineRepository) {
        super(jpaStateMachineRepository);
    }

    @Override
    protected JpaRepositoryStateMachine build(StateMachineContext<S, E> context, Object contextObj, byte[] serialisedContext) {
        JpaRepositoryStateMachine build = super.build(context, contextObj, serialisedContext);
        build.setMachineId(contextObj.toString());
        return build;
    }
}
