# General
- [ ] Unit Tests

# Client
- [x] Replace google.commons.Cache in net handler with custom cache to notify elements of timeouts
- [x] Handle timeouts appropriately (schedule reconnect, notify api)
- [x] Test getLoadBalancedServer, getServersOfType and getAllServers methods in JRPCClient
- [x] Queue Messages if channel is not open

# Core
- [x] Better logging - maybe colorize logging output
- [x] Rewrite protocol codec
- [x] Fix JRPCMessageBuilder not passing conversation parameter to JRPCMessage constructor
- [x] Don't use console colors in file logs

# Examples
### Example Bukkit Plugin
- [x] Locate Player across network
- [x] List Registered client instances (LOAD_BALANCED, TYPE, ALL)
- [x] Complex Object Conversation/Showcase usage of DTOs

# Documentation
- [ ] Bukkit Api Docs
- [ ] Client Api Docs

# GitHub
- [ ] Issue template
- [x] CI Runner
