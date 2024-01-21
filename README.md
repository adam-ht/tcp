# tcp-shutdown
 Minecraft plugin, which shuts down minecraft server if packet is sent to specified port

This plugin is made by AI and changed for my purposes.

When you add this plugin to the plugins folder, and then turn the server on, the plugin should add config.yaml inside plugins/tcp folder. There you can set port, message which should turn the server off and timeout after which the server will turn off

You can define which IP address can send poweroff packet, if you define 0.0.0.0 everybody will be able to shutdown the server

To test if plugin works just download packetsender and send packet to the port you set, and with the same message you set
