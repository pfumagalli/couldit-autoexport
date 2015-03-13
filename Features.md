### What is the AutoExport plugin? ###

AutoExport is a plugin for Confluence 2.x, keeping the content edited through the wiki interface in sync with a copy stored into a directory, appropriately converted in HTML, so that it can be directly plugged into your Apache web server.

Technically, the plugin core is implemented using a listener to Confluence events. Once a page is created or updated, the listener detects this event, and triggers an "export" operation (much like the same "export" you would normally trigger manually from Confluence's menus) and save a copy of the content you just edited as HTML on the disk.

### So, is it just it? ###

Well, not really. I mean, exporting content is fine, but what are the other things that I wanted to address by writing a plugin, rather than going with the way CodeHaus works? Here are some of the features I tried to implement within AutoExport, and a little rationale behind them:

  * **Extreme ease-of-use:** I don't want to install scripts, modify crons, run Perl. I simply want to upload my plugin and be able to configure it as I would with Confluence: through an easy-to-use web interface.

  * **Fine-grained export control and content protection:** By directly integrating export operations with the Confluence user management, AutoExport will detect what can (or can not) be exported, and rewrite all links automatically.

  * **Different look-and-feel for editing and browsing:** Don't get me wrong, Confluence is fantastic when it comes to its UI, but it's tailored for edititing content, not for browsing. When browsing content one might want a lighter and simpler user interface (or you might just want to stick ads in there!)

  * **Nice URLs:** Working for a publication company, I noticed that nice URLs work better both with search engines and users (easier to remember, easier to parse, easier to search). AutoExport will mangle your page titles to look nicer on your URL bar.