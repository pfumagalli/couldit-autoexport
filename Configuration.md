Once installed, the AutoExport plugin will seamlessly integrate with Confluence's configuration system. Once configured you'll even forget you ever installed it (apart from seeing that your server's load average dropped to basically nothing when your site is listed on Slashdot.ORG).

To install AutoExport, like any other plugin, upload the jar plugin itself. Once you have installed it, the plugin manager will show an extra option alongside the "Remove plugin" and "Uninstall plugin" ones. To start your configuration, click on "Configure plugin".

Once in the main configuration screen you will be presented with three main configuration areas:

### Main Configuration ###

  * **Export Root Directory:** This is the directory that AutoExport will use to write all the content from confluence. In this directory, each space will be visible as a subdirectory (therefore, if your space key is "`Test Space`" AutoExport will create a "`test-space`" subdirectory), and in each of those, AutoExport will write all the different pages from Confluence.

  * **Confluence User:** This option specified the user name that AutoExport will use to verify permissions when exporting content. As an example, on this site I created the "`public`" Confluence user. This is only allowed to view certain spaces (no, not the private one), and the "`anonymous`" user can't even _see_ the content. For each page, AutoExport will verify that the user you specify here has the ability to view the page, if so, it will export it, otherwise, it will rewrite all links pointing to it to Confluence itself (so you click on the link, you're prompted to log-in, then you got to the page!)

  * **Export Encoding:** As your web server (your Apache serving pages from the root directory above) might be configured in a different way than your Confluence, here you can enter the encoding in which you want your files to be written to. AutoExport will always use this encoding when exporting Content (but attachment will be copied byte-by-byte, regardless of their encoding).

### Rebuild exported spaces ###

If something gets out of sync, or you just want to export the whole space (including its css, all pages, everything), select a number of spaces and run the manual rebuild operation.

This will behave exactly like changing all the pages in the selected spaces, and it will interactively present you a log of everything that happened.

Oh, and since your auto-export operation might be extremely long, the log will "grow" (call it "web-two-point-zero") while it's being generated (no timeouts, even if your full export lasts 3 hours!!!)

### AutoExport Templates Management ###

Templates for the content AutoExport will process can be managed through this options. You can create, delete, modify all the templates (one for each space, so that each space can have a different look-and-feel) using the same language you are used to create all other Confluence templates.

This allows you to have a completely different look and feel for your editors (using confluence directly), for your users (browsing the Confluence documentation you wrote, exported, and shipped with your latest CD), and all the rest of the lurkers coming from the web.