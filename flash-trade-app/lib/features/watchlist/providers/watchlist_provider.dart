import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/network/dio_client.dart';
import '../data/watchlist_repository.dart';
import '../domain/watchlist_item.dart';

final watchlistRepositoryProvider = Provider<WatchlistRepository>((ref) {
  return WatchlistRepository(DioClient.instance.dio);
});

final watchlistProvider = FutureProvider<List<WatchlistItem>>((ref) {
  return ref.read(watchlistRepositoryProvider).getWatchlist();
});
