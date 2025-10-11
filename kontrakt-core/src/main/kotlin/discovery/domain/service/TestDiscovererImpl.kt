package discovery.domain.service

import discovery.spi.ClasspathScanner

class TestDiscovererImpl(
    private val scanner: ClasspathScanner,
)
