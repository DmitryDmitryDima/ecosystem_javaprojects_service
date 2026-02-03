package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import org.springframework.stereotype.Service;


// данная система работает в совокупности с механизмом waiting for
// юзер, проектируя цепочку, должен создать триггер, который будет активировать какими-либо действиями из ui.
// Активация триггера позволяет перевести ожидающий Outbox event из состояния waiting_for_external в waiting
// при устаревании outbox автоматически запускается компенсационное событие, сигнализирующее, что условие для запуска следующего шага не выполнено
@Service
public class TriggersAggregator {


}
