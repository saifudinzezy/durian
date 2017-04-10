package co.mailtarget.durian

import co.mailtarget.durian.extractor.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URL

/**
 *
 * @author masasdani
 * @since 4/5/17
 */
class WebExtractor: Connection()  {

    var cleaner: DocumentCleaner = DocumentCleaner()
    var strategy = Strategy.META

    /**
     * attemp to extract web data
     *
     * @param url
     * @return
     */
    fun extract(url: String): WebPage {
        val document = getDocument(url)
        when (strategy) {
            Strategy.CONTENT -> return extractContent(url, document)
            Strategy.HYBRID -> return extractHybrid(url, document)
            else -> return extractMeta(url, document)
        }
    }

    private fun extractContent(url: String, document: Document): WebPage {
        val doc = cleaner.clean(document)
        val contentElement = extractContentElement(doc)
        val title = TitleExtractor.getTitleFromContent(doc, document.body())
        val webPage = WebPage(url, title)
        webPage.favicon = FaviconExtractor.getFavicon(document)
        webPage.image = ImageExtractor.getImageFromContent(document, URL(url), webPage.title, contentElement)
        webPage.description = SnippetExtractor.getDescriptionFromContent(document, contentElement)
        webPage.publishedDate = DateExtractor.getDateFromContent(document, contentElement)
        webPage.keywords = KeywordExtractor.getKeywordsFromContent(document, contentElement)
        webPage.content = contentElement
        return webPage
    }

    private fun extractHybrid(url: String, document: Document): WebPage {
        val doc = cleaner.clean(document)
        val contentElement = extractContentElement(doc)
        val title = TitleExtractor.getTitle(doc, document.body())
        val webPage = WebPage(url, title)
        webPage.favicon = FaviconExtractor.getFavicon(document)
        webPage.image = ImageExtractor.getImage(document, URL(url), webPage.title, contentElement)
        webPage.description = SnippetExtractor.getDescription(document, contentElement)
        webPage.publishedDate = DateExtractor.getDate(document, contentElement)
        webPage.keywords = KeywordExtractor.getKeywords(document, contentElement)
        webPage.content = contentElement
        return webPage
    }

    private fun extractMeta(url: String, document: Document): WebPage {
        val title = TitleExtractor.getTitleFromMeta(document)
        val webPage = WebPage(url, title)
        webPage.favicon = FaviconExtractor.getFavicon(document)
        webPage.image = ImageExtractor.getImageFromMeta(document)
        webPage.description = SnippetExtractor.getDescriptionFromMeta(document)
        webPage.publishedDate = DateExtractor.getDateFromMeta(document)
        webPage.keywords = KeywordExtractor.getKeywordsFromMeta(document)
        webPage.content = document.body()
        return webPage
    }

    private fun extractContentElement(document: Document): Element {
        return ContentExtractor.getContentElement(document)
    }

    object Builder {

        private val extractor = WebExtractor()

        fun strategy(strategy: Strategy): Builder {
            extractor.strategy = strategy
            return this
        }

        fun cleanerOptions(options: ArrayList<DocumentCleaner.Options>): Builder {
            extractor.cleaner.options.addAll(options)
            return this
        }

        fun build(): WebExtractor {
            return extractor
        }
    }

    enum class Strategy {
        META,
        CONTENT,
        HYBRID
    }
}
