package cn.xudaodao;

import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cn.xudaodao.dao.MzituDao;
import cn.xudaodao.file.WriteFileManager;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class PictureCrawler extends WebCrawler {

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg" + "|png|mp3|mp3|zip|gz))$");

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase(); // 得到小写的url
		return !FILTERS.matcher(href).matches() // 正则匹配，过滤掉我们不需要的后缀文件
				&& href.startsWith("http://www.mzitu.com"); // url必须是http://www.java1234.com/开头，规定站点

	}

	@Override
	public void visit(Page page) {
		final String entryUrl = "http://www.mzitu.com/";
		String url = page.getWebURL().getURL(); // 获取url
		System.out.println("---------------------URL: " + url);
		if (!url.contains(entryUrl) || url.length() == entryUrl.length()) {
			return;
		}

		String pageNumber = null;
		String pageIndex = null;
		// 该图片组中的以一张图片
		boolean isFirstPicture = false;
		// http://www.mzitu.com/105391/3
		String postfix = url.substring(entryUrl.length());
		if (!postfix.contains("/")) {
			pageNumber = postfix;
			isFirstPicture = true;
		} else {
			isFirstPicture = false;
			String[] arr = postfix.split("/");
			if (arr.length > 1) {
				pageNumber = arr[0];
				pageIndex = arr[1];
			}
		}
		try {
			Integer.valueOf(pageNumber);
		} catch (NumberFormatException e) {
			return;
		}
		String groupUrl = entryUrl + pageNumber;

		int index = 0;
		if (pageIndex == null) {
			index = 1;
		} else {
			try {
				index = Integer.valueOf(pageIndex);
			} catch (NumberFormatException e) {
				return;
			}
		}

		if (page.getParseData() instanceof HtmlParseData) {
			final HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			final String html = htmlParseData.getHtml();
			final Document doc = Jsoup.parse(html);

			String title = parseTitle(doc);
			if (isFirstPicture) {
				String category = parseCategory(doc);
				String publishTime = parsePublishTime(doc);
				int count = parseCount(doc);
				MzituDao.updatePictureGroup(groupUrl,title, publishTime, category, count);
				System.out.println(category + "#####" + publishTime + "#####" + count + "#####" + title);
			}
			String picUrl = parsePictureUrl(doc);
			String pageView = parsePageView(doc);
			MzituDao.insertPictureUrl(groupUrl, picUrl, url, pageView, index);

			System.out.println(picUrl + "#####" + url + "#####" + pageView + "#####" + index + "#####" + title);
			// WriteFileManager.getInstance().writeLine(picUrl + "#####" + url + "#####" +
			// pageView + "#####" + index + "#####" + title);
		}

	}

	private String parseCategory(Document doc) {
		Element categoryTag = doc.select("a[rel=category tag]").first();
		if (categoryTag == null) {
			return null;
		}
		// 类型
		String category = categoryTag.text();
		return category;
	}

	private String parseTitle(Document doc) {
		Element titleElement = doc.select("h2[class=main-title]").first();
		if (titleElement == null) {
			return null;
		}
		// 标题
		String title = titleElement.text();
		if (title == null || title.length() == 0) {
			return null;
		}
		if (title.contains("（")) {
			title = title.substring(0, title.indexOf("（"));
		}
		return title;
	}

	private int parseCount(Document doc) {
		// 获取总图片数，同时更新数据库
		int total = 0;
		Element pagenaviElement = doc.select("div[class=pagenavi]").first();
		Elements pageElements = pagenaviElement.select("span");
		if (pageElements != null && pageElements.size() > 2) {
			String count = pageElements.get(pageElements.size() - 2).text();
			try {
				total = Integer.parseInt(count);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return total;
	}

	private String parsePublishTime(Document doc) {
		// 发布时间
		Element mainMetaElement = doc.select("div[class=main-meta]").first();
		Elements mainMetaSpanElements = mainMetaElement.select("span");
		if (mainMetaSpanElements.size() >= 3) {
			// 格式：发布于 2017-11-04 22:14
			String publishTime = mainMetaSpanElements.get(1).text();
			if (publishTime.startsWith("发布于 ")) {
				publishTime = publishTime.substring("发布于 ".length());
				return publishTime;
			}
		}
		return null;
	}

	private String parsePictureUrl(Document doc) {
		Element mainImageDivElement = doc.select("div[class=main-image]").first();
		Element mainImageElement = mainImageDivElement.select("img[src]").first();
		if (mainImageElement == null) {
			return null;
		}
		String imgUrl = mainImageElement.attr("abs:src");
		return imgUrl;
	}

	private String parsePageView(Document doc) {
		Element mainMetaElement = doc.select("div[class=main-meta]").first();
		Elements mainMetaSpanElements = mainMetaElement.select("span");
		if (mainMetaSpanElements.size() >= 3) {
			// 格式：2,126,474次浏览
			String pageView = mainMetaSpanElements.get(2).text();
			if (pageView.endsWith("次浏览")) {
				pageView = pageView.substring(0, pageView.length() - "次浏览".length());
				return pageView;
			}
		}
		return null;
	}
}
