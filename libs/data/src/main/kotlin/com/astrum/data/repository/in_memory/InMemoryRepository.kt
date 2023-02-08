package com.astrum.data.repository.in_memory

import com.astrum.data.repository.Repository

interface InMemoryRepository<T : Any, ID : Any> : Repository<T, ID>
