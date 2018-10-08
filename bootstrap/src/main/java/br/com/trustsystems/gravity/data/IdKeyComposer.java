package br.com.trustsystems.gravity.data;

import br.com.trustsystems.gravity.exceptions.UnRetriableException;

import java.io.Serializable;

public interface IdKeyComposer {
    Serializable generateIdKey() throws UnRetriableException;
}
