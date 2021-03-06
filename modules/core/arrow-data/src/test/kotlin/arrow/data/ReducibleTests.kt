package arrow.data

import arrow.Kind
import arrow.core.Tuple2
import arrow.instances.LongMonoid
import arrow.test.UnitSpec
import arrow.test.laws.ReducibleLaws
import arrow.typeclasses.*
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.shouldBe
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class ReducibleTests : UnitSpec() {
    init {
        val nonEmptyReducible = object : NonEmptyReducible<ForNonEmptyList, ForListK>() {
            override fun FG(): Foldable<ForListK> = ListK.foldable()

            override fun <A> split(fa: Kind<ForNonEmptyList, A>): Tuple2<A, Kind<ForListK, A>> = Tuple2(fa.extract().head, ListK(fa.extract().tail))
        }

        testLaws(ReducibleLaws.laws(
                nonEmptyReducible,
                { n: Int -> NonEmptyList(n, listOf()) },
                Eq.any(),
                Eq.any(),
                Eq.any()))

        "Reducible<NonEmptyList> default size implementation" {
            val nel = NonEmptyList.of(1, 2, 3)
            nonEmptyReducible.size(LongMonoid, nel) shouldBe nel.size.toLong()
        }

        "Reducible<NonEmptyList>" {
            // some basic sanity checks
            val tail = (2 to 10).toList()
            val total = 1 + tail.sum()
            val nel = NonEmptyList(1, tail)
            nonEmptyReducible.reduceLeft(nel, { a, b -> a + b }) shouldBe total
            nonEmptyReducible.reduceRight(nel, { x, ly -> ly.map({ x + it }) }).value() shouldBe (total)
            nonEmptyReducible.reduce(nel) shouldBe total

            // more basic checks
            val names = NonEmptyList.of("Aaron", "Betty", "Calvin", "Deirdra")
            val totalLength = names.all.map({ it.length }).sum()
            nonEmptyReducible.reduceLeftTo(names, { it.length }, { sum, s -> s.length + sum }) shouldBe totalLength
            nonEmptyReducible.reduceMap(names, { it.length }) shouldBe totalLength
        }
    }
}
