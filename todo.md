# General
- [ ] Unit Tests

# Client
- [x] Replace google.commons.Cache in net handler with custom cache to notify elements of timeouts
- [x] Handle timeouts appropriately (schedule reconnect, notify api)
- [ ] Test getLoadBalancedServer, getServersOfType and getAllServers methods in JRPCClient
- [x] Queue Messages if channel is not open

# Core
- [x] Better logging - maybe colorize logging output
- [x] Rewrite protocol codec
- [x] Fix JRPCMessageBuilder not passing conversation parameter to JRPCMessage constructor

# Examples
### Example Bukkit Plugin
- [ ] Locate Player across network
- [ ] List Registered client instances (LOAD_BALANCED, TYPE, ALL)
- [ ] Complex Object Conversation/Showcase usage of DTOs

### Example Client
- [ ] Initialization

# Documentation
- [ ] Bukkit Api Docs
- [ ] Client Api Docs

# GitHub
- [ ] Issue template
- [ ] CI Runner
