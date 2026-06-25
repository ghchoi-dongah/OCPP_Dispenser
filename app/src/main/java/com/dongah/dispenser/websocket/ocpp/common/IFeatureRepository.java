package com.dongah.dispenser.websocket.ocpp.common;

import com.dongah.dispenser.websocket.ocpp.common.feature.Feature;

import java.util.Optional;

public interface IFeatureRepository {
    Optional<Feature> findFeature(Object needle);
}
