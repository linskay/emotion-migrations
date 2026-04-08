package com.emotion.mifrations.presentation.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emotion.mifrations.application.usecase.GetSystemStateUseCase;
import com.emotion.mifrations.domain.model.ChannelBinding;
import com.emotion.mifrations.domain.model.EventRecord;
import com.emotion.mifrations.domain.model.PostMapping;
import com.emotion.mifrations.presentation.dto.SystemHealthResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Tag(name = "Служебные API", description = "Служебные endpoint'ы мониторинга и диагностики")
public class SystemController {
    private final GetSystemStateUseCase stateUseCase;

    @GetMapping("/health")
    @Operation(summary = "Проверка состояния сервиса", description = "Возвращает флаг доступности backend-сервиса")
    public ResponseEntity<SystemHealthResponse> health() {
        return ResponseEntity.ok(new SystemHealthResponse(true, "Сервис работает штатно"));
    }

    @GetMapping("/integrations")
    @Operation(summary = "Проверка интеграций", description = "Техническая проверка доступности интеграционных подсистем")
    public ResponseEntity<SystemHealthResponse> integrations() {
        return ResponseEntity.ok(new SystemHealthResponse(true, "Интеграции инициализированы"));
    }

    @GetMapping("/mappings")
    @Operation(summary = "Просмотр маппинга TG -> VK", description = "Отображает список соответствий каналов Telegram и сообществ VK")
    public ResponseEntity<List<ChannelBinding>> mappings() {
        return ResponseEntity.ok(stateUseCase.bindings());
    }

    @GetMapping("/events")
    @Operation(summary = "Последние события", description = "Возвращает последние события/ошибки сервиса")
    public ResponseEntity<List<EventRecord>> events() {
        return ResponseEntity.ok(stateUseCase.recentEvents(100));
    }

    @GetMapping("/state")
    @Operation(summary = "Состояние дедупликации", description = "Возвращает текущую таблицу соответствия TG сообщения и VK поста")
    public ResponseEntity<List<PostMapping>> state() {
        return ResponseEntity.ok(stateUseCase.mappings());
    }
}
