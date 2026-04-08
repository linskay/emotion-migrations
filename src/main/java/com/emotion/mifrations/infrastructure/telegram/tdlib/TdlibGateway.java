package com.emotion.mifrations.infrastructure.telegram.tdlib;

import java.util.List;

public interface TdlibGateway {
    List<TdlibUpdate> fetchUpdates(int maxItems);
}
