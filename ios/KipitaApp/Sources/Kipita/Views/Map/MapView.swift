import SwiftUI
import MapKit

struct MapView: View {
    @State private var viewModel = MapViewModel()
    @State private var searchText = ""
    @State private var selectedMerchant: BTCMerchantModel?

    var body: some View {
        ZStack(alignment: .top) {
            map

            VStack(spacing: 8) {
                searchBar
                filterPills
                if viewModel.activeFilters.contains(.btc) {
                    btcSourceToggle
                }
            }
            .padding(.horizontal)
            .padding(.top, 60)
        }
        .ignoresSafeArea(edges: .top)
        .task {
            await viewModel.requestUserLocation()
        }
        .sheet(item: $selectedMerchant) { merchant in
            MerchantSheet(merchant: merchant)
        }
    }

    private var map: some View {
        Map(position: .constant(.region(viewModel.region))) {
            // BTC Merchants
            if viewModel.activeFilters.contains(.btc) {
                ForEach(viewModel.filteredMerchants) { merchant in
                    Annotation(merchant.name, coordinate: CLLocationCoordinate2D(
                        latitude: merchant.lat, longitude: merchant.lng)) {
                        Circle()
                            .fill(Color.orange)
                            .frame(width: 16, height: 16)
                            .onTapGesture { selectedMerchant = merchant }
                    }
                }
            }
            // Food
            if viewModel.activeFilters.contains(.food) {
                ForEach(viewModel.foodPlaces) { place in
                    Annotation(place.name, coordinate: CLLocationCoordinate2D(
                        latitude: place.lat, longitude: place.lng)) {
                        Circle().fill(Color.red).frame(width: 12, height: 12)
                    }
                }
            }
            // Cafe
            if viewModel.activeFilters.contains(.cafe) {
                ForEach(viewModel.cafePlaces) { place in
                    Annotation(place.name, coordinate: CLLocationCoordinate2D(
                        latitude: place.lat, longitude: place.lng)) {
                        Circle().fill(Color.yellow).frame(width: 12, height: 12)
                    }
                }
            }
            // Shops
            if viewModel.activeFilters.contains(.shops) {
                ForEach(viewModel.shopPlaces) { place in
                    Annotation(place.name, coordinate: CLLocationCoordinate2D(
                        latitude: place.lat, longitude: place.lng)) {
                        Circle().fill(Color.purple).frame(width: 12, height: 12)
                    }
                }
            }
            UserAnnotation()
        }
        .mapStyle(.standard)
    }

    private var searchBar: some View {
        HStack {
            Image(systemName: "magnifyingglass").foregroundStyle(.secondary)
            TextField("Search address...", text: $searchText)
                .onSubmit {
                    Task { await viewModel.searchAddress(searchText) }
                }
        }
        .padding(10)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 12))
    }

    private var filterPills: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(MapFilter.allCases, id: \.self) { filter in
                    FilterPill(
                        title: filter.rawValue,
                        isActive: viewModel.activeFilters.contains(filter)
                    ) {
                        if viewModel.activeFilters.contains(filter) {
                            viewModel.activeFilters.remove(filter)
                        } else {
                            viewModel.activeFilters.insert(filter)
                        }
                    }
                }
            }
        }
    }

    private var btcSourceToggle: some View {
        Picker("BTC Source", selection: Binding(
            get: { viewModel.btcSource },
            set: { viewModel.btcSource = $0 }
        )) {
            Text("BTCMap").tag(BTCSource.btcmap)
            Text("Cash App").tag(BTCSource.cashapp)
            Text("Both").tag(BTCSource.both)
        }
        .pickerStyle(.segmented)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 8))
    }
}

struct FilterPill: View {
    let title: String
    let isActive: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline).bold()
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(isActive ? Color.accentColor : Color(.systemGray5),
                            in: Capsule())
                .foregroundStyle(isActive ? .white : .primary)
        }
    }
}

struct MerchantSheet: View {
    @Environment(\.dismiss) private var dismiss
    let merchant: BTCMerchantModel

    var body: some View {
        NavigationStack {
            List {
                if let address = merchant.address {
                    LabeledContent("Address", value: address)
                }
                if let phone = merchant.phone {
                    Link(destination: URL(string: "tel:\(phone)")!) {
                        LabeledContent("Phone", value: phone)
                    }
                }
                if let website = merchant.website {
                    Link(destination: URL(string: website)!) {
                        LabeledContent("Website", value: website)
                    }
                }
                LabeledContent("Source", value: merchant.source.rawValue)
            }
            .navigationTitle(merchant.name)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }
}
