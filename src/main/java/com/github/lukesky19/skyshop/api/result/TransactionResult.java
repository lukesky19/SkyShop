package com.github.lukesky19.skyshop.api.result;

import org.jspecify.annotations.NonNull;

/**
 * This record contains the result of a transaction.
 * @param message The message.
 * @param errored If the processing of the transaction failed and should be cancelled if possible.
 * @param sendErrorMessage If the transaction error message from SkyShop should be sent if errored is true
 * @param cancelled If the pre-buy or pre-sell events were cancelled and the transaction should be cancelled.
 */
public record TransactionResult(
        @NonNull String message,
        boolean errored,
        boolean sendErrorMessage,
        boolean cancelled) {}
