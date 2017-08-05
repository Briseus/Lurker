package torille.fi.lurkforreddit.subreddits

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import torille.fi.lurkforreddit.R

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.scrollTo
import android.support.test.espresso.core.deps.guava.base.Preconditions.checkArgument
import android.support.test.espresso.matcher.ViewMatchers.hasDescendant
import android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.filters.LargeTest
import org.hamcrest.Matchers.allOf

/**
 * Tests for the subreddits screen, screen that containt all of your subreddits
 */

@RunWith(AndroidJUnit4::class)
@LargeTest
class SubredditsScreenTest {

    /**
     * A custom [Matcher] which matches an item in a [RecyclerView] by its text.
     *
     *
     *
     *
     * View constraints:
     *
     *  * View must be a child of a [RecyclerView]
     *

     * @param itemText the text to match
     * *
     * @return Matcher that matches text in the given view
     */
    private fun withItemText(itemText: String): Matcher<View> {
        checkArgument(!TextUtils.isEmpty(itemText), "itemText cannot be null or empty")
        return object : TypeSafeMatcher<View>() {
            public override fun matchesSafely(item: View): Boolean {
                return allOf(
                        isDescendantOfA(isAssignableFrom(RecyclerView::class.java)),
                        withText(itemText)).matches(item)
            }

            override fun describeTo(description: Description) {
                description.appendText("is isDescendantOfA RV with text " + itemText)
            }
        }
    }

    /**
     * [ActivityTestRule] is a JUnit [@Rule][Rule] to launch your activity under test.
     *
     *
     *
     *
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    var mSubredditsActivityTestRule: ActivityTestRule<SubredditsActivity> = ActivityTestRule(SubredditsActivity::class.java)

    @Test
    fun clickSubredditView_opensSubredditUi() {
        val subredditName = "science"
        //scroll to subreddit where name is same
        onView(withId(R.id.subreddits_list)).perform(
                scrollTo<SubredditsAdapter.ViewHolder>(hasDescendant(withText(subredditName))))

        onView(withItemText(subredditName)).check(matches(isDisplayed())).perform(click())


    }

}
