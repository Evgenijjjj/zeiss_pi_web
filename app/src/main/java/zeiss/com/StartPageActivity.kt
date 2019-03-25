package zeiss.com

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_start_page.*
import zeiss.com.fragments.DetailFragment
import zeiss.com.fragments.SettingsFragment

class StartPageActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_page)
        navigation.setOnNavigationItemSelectedListener(this)

        showFragment(DetailFragment())
        title = getString(R.string.title_dashboard)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigation_dashboard -> {
                title = getString(R.string.title_dashboard)
                showFragment(DetailFragment())
                true
            }
            R.id.navigation_notifications -> {
                title = getString(R.string.title_settings)
                showFragment(SettingsFragment())
                true
            }
            else -> false
        }
    }

    private fun showFragment(f: Fragment) {
        val fragment = supportFragmentManager.findFragmentByTag(f.javaClass.name)

        if (fragment != null) {
            hideAllFragments()
            supportFragmentManager.beginTransaction().show(fragment).commit()
        }
        else {
            hideAllFragments()
            supportFragmentManager.beginTransaction()
                .add(R.id.start_page_fragment_container, f, f.javaClass.name)
                .commit()
        }
    }

    private fun hideAllFragments() {
        for (f in supportFragmentManager.fragments)
            supportFragmentManager.beginTransaction().hide(f).commit()
    }
}
