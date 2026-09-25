import SwiftUI
   // ActivityKit is not directly used by the View struct itself

   struct MinimalLiveActivityView: View {
       // This view will be given the context by ActivityConfiguration
       let attributes: MinimalActivityAttributes
       let state: MinimalActivityAttributes.ContentState

       var body: some View {
           VStack {
               Text(attributes.activityName)
                   .font(.headline)
                   .foregroundColor(.blue)
               Text(state.displayText)
                   .font(.largeTitle)
                   .foregroundColor(.black)
           }
           .padding()
           // .activityBackgroundTint will be applied where this view is used
       }
   }
