//
//  SOSPicker.m
//  SyncOnSet
//
//  Created by Christopher Sullivan on 10/25/13.
//
//

#import "SOSPicker.h"

@implementation SOSPicker

@synthesize callbackId;

- (void) getPictures:(CDVInvokedUrlCommand *)command {
//	NSDictionary *options = [command.arguments objectAtIndex: 0];

//	NSInteger maximumImagesCount = [[options objectForKey:@"maximumImagesCount"] integerValue];
//	self.width = [[options objectForKey:@"width"] integerValue];
//	self.height = [[options objectForKey:@"height"] integerValue];
//	self.quality = [[options objectForKey:@"quality"] integerValue];
	
//	if (nil == result) {
//		result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:resultStrings];
//	}
//
//	[self.viewController dismissViewControllerAnimated:YES completion:nil];
//	[self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
    
    TZImagePickerController *imagePickerVc = [[TZImagePickerController alloc] initWithMaxImagesCount:9 delegate:self];

    // You can get the photos by block, the same as by delegate.
    [imagePickerVc setDidFinishPickingPhotosHandle:^(NSArray<UIImage *> *photos, NSArray *assets, BOOL isSelectOriginalPhoto) {

    }];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.viewController presentViewController:imagePickerVc animated:YES completion:nil];
    });
    
}

@end
