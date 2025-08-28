package org.carecode.docpay.util;

import org.carecode.docpay.model.User;

public final class Session {
    private static User currentUser;
    private Session() {}
    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User user) { currentUser = user; }
}

