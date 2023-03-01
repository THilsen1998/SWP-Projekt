package de.unibremen.swp2.security;

import de.unibremen.swp2.model.Role;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to protect classes and methods from being accessed by users with
 * insufficient authorizations, i.e., only the specified roles (see
 * {@link Role}) are allowed to access a certain method or all methods of a
 * certain class.
 */

@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalSecure {

    /**
     * Roles allowed.
     *
     * @return
     *      Roles allowed.
     */
    @Nonbinding
    Role[] roles() default {};
}
