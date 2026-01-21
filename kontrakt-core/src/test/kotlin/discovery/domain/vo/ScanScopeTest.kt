package discovery.domain.vo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ScanScopeTest {

    // -- ScanScope.All --

    @Test
    fun `All - is a singleton object`() {
        val scope1 = ScanScope.All
        val scope2 = ScanScope.All

        assertThat(scope1).isSameAs(scope2)
        assertThat(scope1).isEqualTo(scope2)
    }

    // -- ScanScope.Packages --

    @Test
    fun `Packages - holds package names correctly`() {
        val packages = setOf("com.example", "com.test")
        val scope = ScanScope.Packages(packages)

        assertThat(scope.packageNames).containsExactlyInAnyOrder("com.example", "com.test")
    }

    @Test
    fun `Packages - verify equality`() {
        val set1 = setOf("pkg.a", "pkg.b")
        val set2 = setOf("pkg.a", "pkg.b")
        val set3 = setOf("pkg.c")

        val scope1 = ScanScope.Packages(set1)
        val scope2 = ScanScope.Packages(set2)
        val scope3 = ScanScope.Packages(set3)

        assertThat(scope1).isEqualTo(scope2)
        assertThat(scope1).isNotEqualTo(scope3)
        assertThat(scope1.hashCode()).isEqualTo(scope2.hashCode())
    }

    // -- ScanScope.Classes --

    @Test
    fun `Classes - holds class names correctly`() {
        val classes = setOf("com.example.TestA", "com.example.TestB")
        val scope = ScanScope.Classes(classes)

        assertThat(scope.classNames).containsExactlyInAnyOrder("com.example.TestA", "com.example.TestB")
    }

    @Test
    fun `Classes - verify equality`() {
        val set1 = setOf("ClassA", "ClassB")
        val set2 = setOf("ClassA", "ClassB")
        val set3 = setOf("ClassC")

        val scope1 = ScanScope.Classes(set1)
        val scope2 = ScanScope.Classes(set2)
        val scope3 = ScanScope.Classes(set3)

        assertThat(scope1).isEqualTo(scope2)
        assertThat(scope1).isNotEqualTo(scope3)
        assertThat(scope1.hashCode()).isEqualTo(scope2.hashCode())
    }
}