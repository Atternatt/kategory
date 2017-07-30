package kategory

/**
 * https://www.youtube.com/watch?v=wvSP5qYiz4Y
 */
interface ComposedType<out F, out G>

@Suppress("UNCHECKED_CAST")
fun <F, G, A> HK<F, HK<G, A>>.lift(): HK<ComposedType<F, G>, A> =
        this as HK<ComposedType<F, G>, A>

@Suppress("UNCHECKED_CAST")
fun <F, G, A> HK<ComposedType<F, G>, A>.lower(): HK<F, HK<G, A>> =
        this as HK<F, HK<G, A>>

interface ComposedFoldable<in F, in G> :
        Foldable<ComposedType<F, G>> {

    fun FF(): Foldable<F>

    fun GF(): Foldable<G>

    override fun <A, B> foldL(fa: HK<ComposedType<F, G>, A>, b: B, f: (B, A) -> B): B =
            FF().foldL(fa.lower(), b, { bb, aa -> GF().foldL(aa, bb, f) })

    fun <A, B> foldLC(fa: HK<F, HK<G, A>>, b: B, f: (B, A) -> B): B =
            foldL(fa.lift(), b, f)

    override fun <A, B> foldR(fa: HK<ComposedType<F, G>, A>, lb: Eval<B>, f: (A, Eval<B>) -> Eval<B>): Eval<B> =
            FF().foldR(fa.lower(), lb, { laa, lbb -> GF().foldR(laa, lbb, f) })

    fun <A, B> foldRC(fa: HK<F, HK<G, A>>, lb: Eval<B>, f: (A, Eval<B>) -> Eval<B>): Eval<B> =
            foldR(fa.lift(), lb, f)

    companion object {
        inline operator fun <reified F, reified G> invoke(FF: Foldable<F> = foldable<F>(), GF: Foldable<G> = foldable<G>()): kategory.ComposedFoldable<F, G> =
                kategory.ComposedFoldable(FF, GF)
    }
}

inline fun <F, reified G> Foldable<F>.compose(GT: Foldable<G> = foldable<G>()): ComposedFoldable<F, G> = object :
        ComposedFoldable<F, G> {
            override fun FF(): Foldable<F> = this@compose

            override fun GF(): Foldable<G> = GT
        }

interface ComposedTraverse<F, G> :
        Traverse<ComposedType<F, G>>,
        ComposedFoldable<F, G> {

    fun FT(): Traverse<F>

    fun GT(): Traverse<G>

    fun GA(): Applicative<G>

    override fun <H, A, B> traverse(fa: HK<ComposedType<F, G>, A>, f: (A) -> HK<H, B>, HA: Applicative<H>): HK<H, HK<ComposedType<F, G>, B>> =
            HA.map(FT().traverse(fa.lower(), { ga -> GT().traverse(ga, f, HA) }, HA), { it.lift() })

    fun <H, A, B> traverseC(fa: HK<F, HK<G, A>>, f: (A) -> HK<H, B>, HA: Applicative<H>): HK<H, HK<ComposedType<F, G>, B>> =
            traverse(fa.lift(), f, HA)

    companion object {
        inline operator fun <reified F, reified G> invoke(FF: Traverse<F> = traverse<F>(), GF: Traverse<G> = traverse<G>(), GA: Applicative<G> = applicative<G>()): ComposedTraverse<F, G> =
                ComposedTraverse(FF, GF, GA)
    }
}

inline fun <reified F, reified G> Traverse<F>.compose(FT: Traverse<F> = traverse<F>(), GT: Traverse<G> = traverse<G>(), GA: Applicative<G> = applicative<G>()): Traverse<ComposedType<F, G>> = object :
        ComposedTraverse<F, G> {
            override fun FF(): Foldable<F> = FT

            override fun GF(): Foldable<G> = GT

            override fun FT(): Traverse<F> = this@compose

            override fun GT(): Traverse<G> = GT

            override fun GA(): Applicative<G> = GA
        }