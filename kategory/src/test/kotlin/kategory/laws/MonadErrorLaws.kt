package kategory

import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll

object MonadErrorLaws {

    inline fun <reified F> laws(M: MonadError<F, Throwable> = monadError<F, Throwable>(), EQ: Eq<HK<F, Int>>): List<Law> =
            MonadLaws.laws(M, EQ) + listOf(
                    Law("Monad Error Laws: left zero", { monadErrorLeftZero(M, EQ) }),
                    Law("Monad Error Laws: ensure consistency", { monadErrorEnsureConsistency(M, EQ) })
            )

    inline fun <reified F> monadErrorLeftZero(M: MonadError<F, Throwable> = monadError<F, Throwable>(), EQ: Eq<HK<F, Int>>): Unit =
            forAll(genFunctionAToB<Int, HK<F, Int>>(genApplicative(Gen.int(), M)), genThrowable(), { f: (Int) -> HK<F, Int>, e: Throwable ->
                M.flatMap(M.raiseError<Int>(e), f).equalUnderTheLaw(M.raiseError<Int>(e), EQ)
            })

    inline fun <reified F> monadErrorEnsureConsistency(M: MonadError<F, Throwable> = monadError<F, Throwable>(), EQ: Eq<HK<F, Int>>): Unit =
            forAll(genApplicative(Gen.int(), M), genThrowable(), genFunctionAToB<Int, Boolean>(Gen.bool()), { fa: HK<F, Int>, e: Throwable, p: (Int) -> Boolean ->
                M.ensure(fa, { e }, p).equalUnderTheLaw(M.flatMap(fa, { a -> if (p(a)) M.pure(a) else M.raiseError(e) }), EQ)
            })

}
