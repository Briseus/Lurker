package torille.fi.lurkforreddit.di.scope;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by eva on 24.4.2017.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface FragmentScoped {
}
