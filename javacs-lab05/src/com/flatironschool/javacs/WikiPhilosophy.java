package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;

public class WikiPhilosophy {
	
	final static WikiFetcher wf = new WikiFetcher();
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		List<String> visited = new ArrayList<String>();
		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";

		while (true) {
			visited.add(url);
			url = getFirstLink(url);
			if (url == null) {
				System.out.println("Failed! The page has no links.");
				return;
			}
			if (visited.contains(url)) {
				System.out.println("Failed! Got stuck in a loop.");
				return;
			}
			if (url.equals("https://en.wikipedia.org/wiki/Philosophy")) {
				// +1 to include the Philosophy page.
				System.out.println("Success! Got to Philosophy after visiting " + (visited.size() + 1) + " pages.");
				return;
			}
		}

	}


	private static String getFirstLink(String url) throws IOException {
		Elements paragraphs = wf.fetchWikipedia(url);
		int openParen = 0;
		int closingParen = 0;

		for (Element paragraph: paragraphs) {
			Iterable<Node> iter = new WikiNodeIterable(paragraph);

			for (Node node: iter) {

				// To keep number of open and closed parenthesis
				if (node instanceof TextNode) {
					String text = ((TextNode) node).text();
					for (int i = 0; i < text.length(); i++) {
						if (text.charAt(i) == '(') {
							openParen++;
						} else if (text.charAt(i) == ')') {
							closingParen++;
						}
					}
				}

				// If there is a link and it is not inside a parenthesis and it is not italicized
				if (node instanceof Element && ((Element) node).tagName().equals("a") && openParen <= closingParen &&
						!isItalicized(node)) {
					String link = ((Element) node).attr("href");
					if (link.length() > 6 && link.substring(0, 6).equals("/wiki/")) {
						return "https://en.wikipedia.org" + link;
					}
				}

			}
		}
		return null;
	}


	private static Boolean isItalicized(Node node) {
		Boolean isItalicized = false;
		Node parent = node.parent();
		while (parent != null && !isItalicized) {
			if (parent instanceof Element && ((Element) parent).tagName().equals("i")) {
				isItalicized = true;
			} else {
				parent = parent.parent();
			}
		}
		return isItalicized;
	}
}
