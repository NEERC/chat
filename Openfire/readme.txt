= NEERC plugin for Openfire server =

== Build instructions ==


Get Openfire source code:
http://download.igniterealtime.org/openfire/openfire_src_3_6_4.zip (or .tar.gz)

Extract it somewhere (%src_dir%)

Copy plugins/neerc dir to %src_dir%/plugins/neerc

cd %src_dir%/build
ant plugins

Copy %src_dir%/target/openfire/plugins/neerc.jar to plugins dir in your Openfire installation

It now should load & start automatically.

== Upgrading ==

Just overwrite old neerc.jar with new version and wait few seconds.
If it fails to reload this way, stop and restart openfire server.

== Debugging ==

Enable debug log in openfire:
Openfire web admin > Server > Logs > Debug > Debug Log: Enabled

Then look for logs/debug.log in openfire dir.