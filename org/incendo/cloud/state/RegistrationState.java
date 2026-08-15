package org.incendo.cloud.state;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(status = Status.STABLE)
public enum RegistrationState implements State {
   BEFORE_REGISTRATION,
   REGISTERING,
   AFTER_REGISTRATION;
}
